package spore

import (
	"context"
	"database/sql"
	"encoding/json"
	"errors"
	"strings"
	"time"

	"github.com/SethCurry/mooshroom/internal/models"
	"github.com/SethCurry/mooshroom/internal/mqtt"
	paho "github.com/eclipse/paho.mqtt.golang"
	"go.uber.org/zap"
)

type DHTDataMessage struct {
	Pin         int     `json:"pin"`
	Temperature float32 `json:"temperature"`
	Humidity    float32 `json:"humidity"`
}

func NewMQTTListener(db *models.Client, prefix string) *MQTTListener {
	return &MQTTListener{
		db:     db,
		prefix: prefix,
	}
}

type MQTTListener struct {
	db     *models.Client
	prefix string
}

func (m *MQTTListener) ProcessDHTMessage(mqttClient *mqtt.Client, message paho.Message, logger *zap.Logger) {
	topic := message.Topic()
	topicWithoutPrefix := strings.TrimPrefix(topic, m.prefix+"/")
	splitTopic := strings.Split(topicWithoutPrefix, "/")

	if len(splitTopic) != 3 {
		logger.Error("message topic does not look like a DHT data message", zap.String("topic", topic))
		return
	}

	if splitTopic[2] != "dht_data" {
		logger.Error("message topic is not for DHT data", zap.String("topic", topic))
		return
	}

	payload := message.Payload()

	var data DHTDataMessage

	err := json.Unmarshal(payload, &data)
	if err != nil {
		logger.Error("failed to unmarshal JSON in dht data message", zap.String("json", string(payload)), zap.Error(err))
		return
	}

	sporeName := splitTopic[1]

	sporeID, err := m.db.Spores().GetIDByName(context.Background(), sporeName)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			newID, err := m.db.Spores().Create(context.Background(), sporeName)
			if err != nil {
				logger.Error("no existing spore found, and failed to create one", zap.String("name", sporeName), zap.Error(err))
				return
			}
			sporeID = newID
		} else {
			logger.Error("failed to get spore by name", zap.String("name", sporeName), zap.Error(err))
			return
		}
	}

	dhtSensorID, err := m.db.DHTSensors().GetIDBySporeIDAndPin(context.Background(), sporeID, data.Pin)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			newID, err := m.db.DHTSensors().Create(context.Background(), sporeName+"-dht", data.Pin, sporeID)
			if err != nil {
				logger.Error("no existing DHT sensor and failed to create one", zap.String("spore_name", sporeName), zap.Int("spore_id", sporeID), zap.Int("pin", data.Pin), zap.Error(err))
				return
			}
			dhtSensorID = newID
		} else {
			logger.Error("failed to get DHT sensor ID", zap.String("spore_name", sporeName), zap.Int("spore_id", sporeID), zap.Int("pin", data.Pin), zap.Error(err))
			return
		}
	}

	err = m.db.DHTSensorData().Create(context.Background(), time.Now(), dhtSensorID, data.Humidity, data.Temperature)
	if err != nil {
		logger.Error("failed to create DHT sensor record", zap.Int("dht_sensor_id", dhtSensorID), zap.Error(err))
	} else {
		logger.Debug("created DHT sensor record", zap.Int("dht_sensor_id", dhtSensorID), zap.Float32("humidity", data.Humidity), zap.Float32("temperature", data.Temperature))
	}
}
