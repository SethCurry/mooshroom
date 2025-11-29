package endpoints

import (
	"errors"
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
	endTime := time.Now()
	startTime := time.Now().Add(time.Hour * -24)
	sensorIDStr := req.Params.ByName("dht_sensor_id")

	if startTimeParam := req.Request.URL.Query().Get("start"); startTimeParam != "" {
		startOffset, err := strconv.Atoi(startTimeParam)
		if err != nil {
			return fmt.Errorf("start parameter could not be parsed to an integer: %w", err)
		}

		startTime = time.Now().Add(time.Second * -1 * time.Duration(startOffset))
	}

	if endTimeParam := req.Request.URL.Query().Get("end"); endTimeParam != "" {
		endOffset, err := strconv.Atoi(endTimeParam)
		if err != nil {
			return fmt.Errorf("end parameter could not be parsed to an integer: %w", err)
		}

		endTime = time.Now().Add(time.Second * -1 * time.Duration(endOffset))
	}

	if endTime.Unix() < startTime.Unix() {
		return errors.New("end time is before start time")
	}

	sensorID, err := strconv.Atoi(sensorIDStr)
	if err != nil {
		return fmt.Errorf("dht_sensor_id in URL is not a valid integer: %q: %w", sensorIDStr, err)
	}

	readings, err := req.DB.DHTSensorData().ListForSensorID(req.Context(), sensorID, startTime, endTime)
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

	return req.JSONResponse(200, response)
}
