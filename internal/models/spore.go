package models

import (
	"context"
	"fmt"

	"github.com/Masterminds/squirrel"
)

type SporeClient struct {
	client *Client
}

type SporeQueryResultDHTSensor struct {
	ID   string
	Name string
}

type SporeQueryResult struct {
	ID   int
	Name string
}

func (s *SporeClient) Query(ctx context.Context) ([]*SporeQueryResult, error) {
	query, vars, err := s.client.builder.Select("id", "name").From("spores").ToSql()
	if err != nil {
		return nil, fmt.Errorf("failed to build query to list spores: %w", err)
	}

	rows, err := s.client.conn.Query(ctx, query, vars...)
	if err != nil {
		return nil, fmt.Errorf("failed to execute SQL to list spores: %w", err)
	}
	defer rows.Close()

	var results []*SporeQueryResult

	for rows.Next() {
		var id int
		var name string

		err = rows.Scan(&id, &name)
		if err != nil {
			return nil, fmt.Errorf("failed while scanning row in spore list: %w", err)
		}

		results = append(results, &SporeQueryResult{
			ID:   id,
			Name: name,
		})
	}

	return results, nil
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

type GetSporeDHTSensor struct {
	ID   int
	Name string
	Pin  int
}

type GetSporeResult struct {
	ID         int
	Name       string
	DHTSensors []*GetSporeDHTSensor
}

func (s *SporeClient) GetByID(ctx context.Context, id int) (*GetSporeResult, error) {
	query, vars, err := s.client.builder.Select("name").From("spores").Where(squirrel.Eq{"id": id}).ToSql()
	if err != nil {
		return nil, fmt.Errorf("failed to build query to list spores: %w", err)
	}

	var name string

	err = s.client.conn.QueryRow(ctx, query, vars...).Scan(&name)
	if err != nil {
		return nil, fmt.Errorf("failed to execute SQL to list spores: %w", err)
	}

	sensors, err := s.client.DHTSensors().ListSensorsForSporeID(ctx, id)
	if err != nil {
		return nil, fmt.Errorf("failed to list sensors for spore %d: %w", id, err)
	}

	resultSensors := make([]*GetSporeDHTSensor, len(sensors))
	for k, v := range sensors {
		resultSensors[k] = &GetSporeDHTSensor{
			ID:   v.ID,
			Name: v.Name,
			Pin:  v.Pin,
		}
	}

	return &GetSporeResult{
		ID:         id,
		Name:       name,
		DHTSensors: resultSensors,
	}, nil
}
