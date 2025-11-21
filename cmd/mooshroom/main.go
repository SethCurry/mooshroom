package main

import (
	"context"
	"fmt"
	"net/http"
	"os"

	"github.com/SethCurry/mooshroom/internal/mooshroom"
	"github.com/SethCurry/mooshroom/internal/mqtt"
	paho "github.com/eclipse/paho.mqtt.golang"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"github.com/urfave/cli/v3"
	"go.uber.org/zap"
)

type CLITools struct {
	logger       *zap.Logger
	parsedConfig *mooshroom.Config
}

func (t *CLITools) getConfig() (*mooshroom.Config, error) {
	if t.parsedConfig == nil {
		config, err := mooshroom.ParseConfigFile("./config.yaml")
		if err != nil {
			return nil, err
		}
		t.parsedConfig = config
	}

	return t.parsedConfig, nil
}

func (t *CLITools) getMQTTClient(clientID string) (*mqtt.Client, error) {
	config, err := t.getConfig()
	if err != nil {
		return nil, err
	}

	opts := []mqtt.ClientOption{
		mqtt.WithBrokers(config.MQTT.Brokers...),
		mqtt.WithClientID(clientID),
		mqtt.WithLogger(t.logger),
	}
	if config.MQTT.Username != "" {
		opts = append(opts, mqtt.WithUsername(config.MQTT.Username))
	}
	if config.MQTT.Password != "" {
		opts = append(opts, mqtt.WithPassword(config.MQTT.Password))
	}
	return mqtt.NewClient(opts...)
}

func main() {
	logger, err := zap.NewDevelopment()
	if err != nil {
		panic(err)
	}

	tools := &CLITools{
		logger: logger,
	}

	cmd := &cli.Command{
		Name:  "mooshroom",
		Usage: "Mooshroom is a tool for growing culinary mushrooms.",
		Commands: []*cli.Command{
			{
				Name:  "server",
				Usage: "Start the HTTP server and MQTT listener.",
				Action: func(ctx context.Context, cmd *cli.Command) error {
					config, err := tools.getConfig()
					if err != nil {
						return err
					}

					http.Handle("/metrics", promhttp.Handler())
					http.ListenAndServe(fmt.Sprintf(":%d", config.HTTP.Port), nil)

					return nil
				},
			},
			{
				Name:  "tools",
				Usage: "Various tools for testing and debugging.",
				Commands: []*cli.Command{
					{
						Name:  "mqtt",
						Usage: "Test the MQTT connection and subscriptions.",
						Commands: []*cli.Command{
							{
								Name:  "subscribe",
								Usage: "Subscribe to a topic and print the messages.",
								Action: func(ctx context.Context, cmd *cli.Command) error {
									numArgs := cmd.Args().Len()
									topics := make([]string, numArgs)
									for i := 0; i < numArgs; i++ {
										topics[i] = cmd.Args().Get(i)
									}
									client, err := tools.getMQTTClient("cli-mqtt-subscribe")
									if err != nil {
										return err
									}
									logger.Debug("got MQTT client")

									client.AddHandler(mqtt.MessageHandler{
										Callback: func(c *mqtt.Client, msg paho.Message, callLogger *zap.Logger) {
											tools.logger.Info("saw MQTT message", zap.String("topic", msg.Topic()), zap.String("payload", string(msg.Payload())))
										},
									})

									for _, topic := range topics {
										logger.Info("subscribing to topic", zap.String("topic", topic))
										err := client.Subscribe(topic, mqtt.QoS0)
										if err != nil {
											logger.Error("failed to subscribe to topic", zap.String("topic", topic), zap.Error(err))
										}
									}
									logger.Info("subscribed to topics")
									<-ctx.Done()
									return nil
								},
							},
							{
								Name:  "publish",
								Usage: "Publish a message to a topic.",
								Flags: []cli.Flag{
									&cli.StringFlag{
										Name:  "topic",
										Usage: "The topic to publish the message to.",
									},
								},
								Action: func(ctx context.Context, cmd *cli.Command) error {
									numArgs := cmd.Args().Len()
									client, err := tools.getMQTTClient("cli-mqtt-publish")
									if err != nil {
										return err
									}

									topic := cmd.String("topic")

									for i := 0; i < numArgs; i++ {
										err := client.Publish(topic, mqtt.QoS0, false, []byte(cmd.Args().Get(i)))
										if err != nil {
											logger.Error("failed to publish message", zap.String("topic", topic), zap.String("payload", cmd.Args().Get(i)), zap.Error(err))
										}
									}
									return nil
								},
							},
						},
					},
				},
			},
		},
	}
	if err := cmd.Run(context.Background(), os.Args); err != nil {
		logger.Fatal("failed to run command", zap.Error(err))
	}
}
