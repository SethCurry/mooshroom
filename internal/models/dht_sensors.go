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

type ListSensorsForSporeIDResult struct {
	ID   int
	Name string
	Pin  int
}

func (d *DHTSensorClient) ListSensorsForSporeID(ctx context.Context, sporeID int) ([]*ListSensorsForSporeIDResult, error) {
	query, vars, err := d.client.builder.Select("id", "name", "gpio_pin").From("dht_sensors").Where(squirrel.Eq{"spore_id": sporeID}).ToSql()
	if err != nil {
		return nil, fmt.Errorf("failed to build query to get DHT sensors for spore ID %d: %w", sporeID, err)
	}

	rows, err := d.client.conn.Query(ctx, query, vars...)
	if err != nil {
		return nil, fmt.Errorf("failed to execute SQL query to get DHT sensors for spore ID %d: %w", sporeID, err)
	}

	var results []*ListSensorsForSporeIDResult

	for rows.Next() {
		var id, pin int
		var name string

		err = rows.Scan(&id, &name, &pin)
		if err != nil {
			return nil, fmt.Errorf("failed while scanning rows for DHT sensors of spore %d: %w", sporeID, err)
		}

		results = append(results, &ListSensorsForSporeIDResult{
			ID:   id,
			Name: name,
			Pin:  pin,
		})
	}

	return results, nil
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
