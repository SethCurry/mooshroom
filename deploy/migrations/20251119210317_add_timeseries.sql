-- +goose Up
-- +goose StatementBegin
CREATE TABLE genera (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE species (
    id SERIAL PRIMARY KEY,
    genus_id INTEGER REFERENCES genera(id),
    name VARCHAR(255) NOT NULL
);

CREATE TABLE species_common_names (
    id SERIAL PRIMARY KEY,
    species_id INTEGER REFERENCES species(id),
    name VARCHAR(255) NOT NULL
);

CREATE TABLE enclosures (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE spores (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL,
  mac_address VARCHAR(12) UNIQUE NOT NULL,
  enclosure_id INTEGER REFERENCES enclosures(id)
);

CREATE TABLE dht_sensors (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  gpio_pin INTEGER NOT NULL,
  spore_id INTEGER REFERENCES spores(id)
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
DROP TABLE enclosures;
DROP TABLE species_common_names;
DROP TABLE species;
DROP TABLE genera;
-- +goose StatementEnd
