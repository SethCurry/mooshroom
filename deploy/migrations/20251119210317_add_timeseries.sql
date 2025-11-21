-- +goose Up
-- +goose StatementBegin
CREATE TABLE spores (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255)
);

CREATE TABLE dht_sensors (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255),
  spore_id INTEGER REFERENCES spores(id),
  mqtt_topic VARCHAR(512)
);

CREATE TABLE dht_data (
  time TIMESTAMPTZ NOT NULL,
  sensor_id INTEGER REFERENCES dht_sensors(id),
  temperature DOUBLE PRECISION,
  humidity DOUBLE PRECISION
) WITH (
  tsdb.hypertable
);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP TABLE dht_data;
DROP TABLE dht_sensors;
DROP TABLE spores;
-- +goose StatementEnd
