package models

import (
	"context"
	"fmt"
	"time"

	"github.com/Masterminds/squirrel"
)

type DHTSensorDataClient struct {
	client *Client
}

func (d *DHTSensorDataClient) Create(ctx context.Context, atTime time.Time, sensorID int, humidity float32, temperature float32) error {
	query, vars, err := d.client.builder.Insert("dht_data").Columns("time", "sensor_id", "temperature", "humidity").Values(atTime, sensorID, temperature, humidity).ToSql()
	if err != nil {
		return fmt.Errorf("failed to generate SQL for inserting DHT data for sensor %d: %w", sensorID, err)
	}

	_, err = d.client.conn.Exec(ctx, query, vars...)
	if err != nil {
		return fmt.Errorf("failed to execute SQL query to insert DHT sensor data: %w", err)
	}

	return nil
}

type DHTSensorData struct {
	Timestamp   time.Time
	Temperature float32
	Humidity    float32
}

func (d *DHTSensorDataClient) ListForSensorID(ctx context.Context, sensorID int) ([]*DHTSensorData, error) {
	query, vars, err := d.client.builder.Select("time", "temperature", "humidity").From("dht_data").Where(squirrel.Eq{"sensor_id": sensorID}).OrderBy("time").ToSql()
	if err != nil {
		return nil, fmt.Errorf("failed to build SQL query to get DHT sensor data for sensor %d: %w", sensorID, err)
	}

	rows, err := d.client.conn.Query(ctx, query, vars...)
	if err != nil {
		return nil, fmt.Errorf("failed to execute SQL query to get DHT sensor data for sensor %d: %w", sensorID, err)
	}

	var result []*DHTSensorData

	for rows.Next() {
		var ts time.Time
		var temperature, humidity float32

		err = rows.Scan(&ts, &temperature, &humidity)
		if err != nil {
			return nil, fmt.Errorf("failed while scanning rows listing DHT sensor data for sensor %d: %w", sensorID, err)
		}

		result = append(result, &DHTSensorData{
			Timestamp:   ts,
			Temperature: temperature,
			Humidity:    humidity,
		})
	}

	return result, nil
}
