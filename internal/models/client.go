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

func (c *Client) Spores() *SporeClient {
	return &SporeClient{
		client: c,
	}
}

type DHTSensorClient struct {
	client *Client
}

func (d *DHTSensorClient) Create(ctx context.Context, name string, pin int, sporeID int) (int, error) {
	query, vars, err := d.client.builder.Insert("dht_sensors").Columns("name", "gpio_pin", "spore_id").Values(name, pin, sporeID).Suffix("RETURNING id").ToSql()
	if err != nil {
		return -1, fmt.Errorf("failed to generate SQL for creating DHT sensor with name %q, pin %d, and spore ID %d: %w", name, pin, sporeID, err)
	}

	var id int

	err = d.client.conn.QueryRow(ctx, query, vars...).Scan(&id)
	if err != nil {
		return -1, fmt.Errorf("failed to create DHT sensor with name %q, pin %d, and spore ID %d: %w", name, pin, sporeID, err)
	}

	return id, nil
}

func (d *DHTSensorClient) GetIDBySporeIDAndPin(ctx context.Context, sporeID int, pin int) (int, error) {
	query, vars, err := d.client.builder.Select("id").From("dht_sensors").Where(squirrel.Eq{"spore_id": sporeID, "gpio_pin": pin}).ToSql()
	if err != nil {
		return -1, fmt.Errorf("failed to create query for DHT sensor by spore ID %d and pin %d: %w", sporeID, pin, err)
	}

	var id int

	err = d.client.conn.QueryRow(ctx, query, vars...).Scan(&id)
	if err != nil {
		return -1, fmt.Errorf("failed to get DHT sensor ID by spore ID %d and pin %d: %w", sporeID, pin, err)
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

type SporeClient struct {
	client *Client
}

func (s *SporeClient) GetIDByName(ctx context.Context, name string) (int, error) {
	query, vars, err := s.client.builder.Select("id").From("spores").Where(squirrel.Eq{"name": name}).ToSql()
	if err != nil {
		return -1, fmt.Errorf("failed to create query for spore by name %q: %w", name, err)
	}

	var id int

	err = s.client.conn.QueryRow(ctx, query, vars...).Scan(&id)
	if err != nil {
		return -1, fmt.Errorf("failed to get spore ID by name %q: %w", name, err)
	}

	return id, nil
}

func (s *SporeClient) Create(ctx context.Context, name string) (int, error) {
	query, vars, err := s.client.builder.Insert("spores").Columns("name").Values(name).Suffix("RETURNING id").ToSql()
	if err != nil {
		return -1, fmt.Errorf("failed to build query to insert spore with name %q: %w", name, err)
	}

	var id int

	err = s.client.conn.QueryRow(ctx, query, vars...).Scan(&id)
	if err != nil {
		return -1, fmt.Errorf("failed to create spore: %w", err)
	}

	return id, nil
}
