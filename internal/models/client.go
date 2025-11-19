package models

import (
	"context"
	"fmt"
	"time"

	"github.com/Masterminds/squirrel"
	"github.com/jackc/pgx/v5"
)

func NewClient(ctx context.Context, dsn string) (*Client, error) {
	conn, err := pgx.Connect(ctx, dsn)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to Postgres: %w", err)
	}

	return &Client{
		builder: squirrel.StatementBuilder.PlaceholderFormat(squirrel.Dollar),
		conn:    conn,
	}, nil
}

type Client struct {
	builder squirrel.StatementBuilderType
	conn    *pgx.Conn
}

func (c *Client) DHTSensors() *DHTSensorClient {
	return &DHTSensorClient{
		client: c,
	}
}

func (c *Client) DHTSensorData() *DHTSensorDataClient {
	return &DHTSensorDataClient{
		client: c,
	}
}

type DHTSensorClient struct {
	client *Client
}

func (d *DHTSensorClient) Create(ctx context.Context, name string, mqttTopic string) (int, error) {
	query, vars, err := d.client.builder.Insert("dht_sensors").Columns("name", "mqtt_topic").Values(name, mqttTopic).Suffix("RETURNING id").ToSql()
	if err != nil {
		return -1, fmt.Errorf("failed to generate SQL for creating DHT sensor with name %q and MQTT topic %q: %w", name, mqttTopic, err)
	}

	var id int

	err = d.client.conn.QueryRow(ctx, query, vars...).Scan(&id)
	if err != nil {
		return -1, fmt.Errorf("failed to create DHT sensor with name %q and MQTT topic %q: %w", name, mqttTopic, err)
	}

	return id, nil
}

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
