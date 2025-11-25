package models

import (
	"context"
	"fmt"

	"github.com/Masterminds/squirrel"
)

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
