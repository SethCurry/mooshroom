package models

import (
	"context"
	"fmt"
	"time"
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
