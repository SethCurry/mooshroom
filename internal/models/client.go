package models

import (
	"context"
	"fmt"

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
