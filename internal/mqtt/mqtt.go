package mqtt

import (
	"fmt"

	paho "github.com/eclipse/paho.mqtt.golang"
	"go.uber.org/zap"
)

type mqttLogger struct {
	printImpl  func(v ...interface{})
	printfImpl func(format string, v ...interface{})
}

func (l *mqttLogger) Println(v ...interface{}) {
	l.printImpl(v...)
}
func (l *mqttLogger) Printf(format string, v ...interface{}) {
	l.printfImpl(format, v...)
}

func ConfigureLogging(logger *zap.Logger) {
	sugaredLogger := logger.Sugar()

	paho.ERROR = &mqttLogger{
		printImpl:  sugaredLogger.Error,
		printfImpl: sugaredLogger.Errorf,
	}

	paho.CRITICAL = &mqttLogger{
		printImpl:  sugaredLogger.Error,
		printfImpl: sugaredLogger.Errorf,
	}

	paho.WARN = &mqttLogger{
		printImpl:  sugaredLogger.Warn,
		printfImpl: sugaredLogger.Warnf,
	}

	paho.DEBUG = &mqttLogger{
		printImpl:  sugaredLogger.Debug,
		printfImpl: sugaredLogger.Debugf,
	}
}

type ClientOption func(*Client) error

func WithBrokers(brokers ...string) ClientOption {
	return func(c *Client) error {
		for _, broker := range brokers {
			c.baseClientOptions.AddBroker(broker)
		}
		return nil
	}
}

func WithClientID(clientID string) ClientOption {
	return func(c *Client) error {
		c.baseClientOptions.SetClientID(clientID)
		return nil
	}
}

func WithUsername(username string) ClientOption {
	return func(c *Client) error {
		c.baseClientOptions.SetUsername(username)
		return nil
	}
}

func WithPassword(password string) ClientOption {
	return func(c *Client) error {
		c.baseClientOptions.SetPassword(password)
		return nil
	}
}

func WithLogger(logger *zap.Logger) ClientOption {
	return func(c *Client) error {
		c.logger = logger
		return nil
	}
}

func WithSubscriptions(subscriptions ...Subscription) ClientOption {
	return func(c *Client) error {
		c.subscriptions = append(c.subscriptions, subscriptions...)
		return nil
	}
}

func NewClient(opts ...ClientOption) (*Client, error) {
	client := &Client{
		baseClientOptions: paho.NewClientOptions(),
		logger:            zap.NewNop(),
		subscriptions:     []Subscription{},
	}

	for _, opt := range opts {
		if err := opt(client); err != nil {
			return nil, fmt.Errorf("failed to apply MQTT client option: %w", err)
		}
	}

	client.baseClientOptions.OnConnect = func(pahoClient paho.Client) {
		client.logger.Info("connected to MQTT broker")
		for _, sub := range client.subscriptions {
			client.Subscribe(sub)
		}
	}

	return client, nil
}

// QoSLevel encapsulates the QoS level for an MQTT subscription.
type QoSLevel byte

const (
	// QoS0 is fire and forget messaging.
	QoS0 QoSLevel = 0

	// QoS1 is at least once messaging.
	QoS1 QoSLevel = 1

	// QoS2 is exactly once messaging.
	QoS2 QoSLevel = 2
)

type Subscription struct {
	Name    string
	Topic   string
	QoS     QoSLevel
	Handler func(client *Client, msg paho.Message, logger *zap.Logger) error
}

type Client struct {
	baseClientOptions *paho.ClientOptions
	baseClient        paho.Client
	logger            *zap.Logger
	subscriptions     []Subscription
}

func (c *Client) Subscribe(sub Subscription) error {
	tok := c.baseClient.Subscribe(sub.Topic, byte(sub.QoS), c.wrapSubscriptionHandler(sub))
	if tok.Wait() && tok.Error() != nil {
		c.logger.Error("failed to subscribe to topic", zap.String("subscription", sub.Name), zap.String("topic", sub.Topic), zap.Error(tok.Error()))
		return fmt.Errorf("failed to subscribe to topic: %w", tok.Error())
	}
	c.logger.Info("subscribed to topic", zap.String("subscription", sub.Name), zap.String("topic", sub.Topic))
	return nil
}

func (c *Client) Publish(topic string, qos QoSLevel, retained bool, payload []byte) error {
	tok := c.baseClient.Publish(topic, byte(qos), retained, payload)
	if tok.Wait() && tok.Error() != nil {
		return fmt.Errorf("failed to publish message: %w", tok.Error())
	} else {
		c.logger.Info("published message", zap.String("topic", topic), zap.String("payload", string(payload)))
	}
	return nil
}

func (c *Client) wrapSubscriptionHandler(sub Subscription) paho.MessageHandler {
	return func(client paho.Client, msg paho.Message) {
		c.logger.Debug("received message", zap.String("subscription", sub.Name), zap.String("topic", msg.Topic()), zap.String("payload", string(msg.Payload())))

		if err := sub.Handler(c, msg, c.logger); err != nil {
			c.logger.Error("failed to handle message", zap.String("subscription", sub.Name), zap.String("topic", msg.Topic()), zap.String("payload", string(msg.Payload())), zap.Error(err))
		}
	}
}

// Connect connects to the MQTT broker.  It will block until the connection is established or an error occurs.
func (c *Client) Connect() error {
	tok := c.baseClient.Connect()

	if tok.Wait() && tok.Error() != nil {
		return fmt.Errorf("failed to connect to MQTT broker: %w", tok.Error())
	}

	return nil
}
