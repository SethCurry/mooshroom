package endpoints

import (
	"encoding/json"
	"fmt"
	"strconv"
	"time"

	"github.com/SethCurry/mooshroom/internal/api"
)

type ListDHTSensorDataResponseItem struct {
	Timestamp   time.Time `json:"timestamp"`
	Temperature float32   `json:"temperature"`
	Humidity    float32   `json:"humidity"`
}

type ListDHTSensorDataResponse struct {
	Readings []*ListDHTSensorDataResponseItem `json:"readings"`
}

func ListDHTSensorData(req *api.RequestContext) error {
	sensorIDStr := req.Params.ByName("dht_sensor_id")

	sensorID, err := strconv.Atoi(sensorIDStr)
	if err != nil {
		return fmt.Errorf("dht_sensor_id in URL is not a valid integer: %q: %w", sensorIDStr, err)
	}

	readings, err := req.DB.DHTSensorData().ListForSensorID(req.Context(), sensorID)
	if err != nil {
		return fmt.Errorf("failed to list DHT sensor data for sensor %d: %w", sensorID, err)
	}

	formattedReadings := make([]*ListDHTSensorDataResponseItem, len(readings))

	for k, v := range readings {
		formattedReadings[k] = &ListDHTSensorDataResponseItem{
			Timestamp:   v.Timestamp,
			Temperature: v.Temperature,
			Humidity:    v.Humidity,
		}
	}

	response := &ListDHTSensorDataResponse{
		Readings: formattedReadings,
	}

	marshalled, err := json.Marshal(response)
	if err != nil {
		return fmt.Errorf("failed to marshal DHT sensor data: %w", err)
	}

	req.Writer.WriteHeader(200)
	req.Writer.Write(marshalled)

	return nil
}
