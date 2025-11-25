package models

import (
	"context"
	"fmt"

	"github.com/Masterminds/squirrel"
)

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
