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
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP TABLE species_common_names;
DROP TABLE species;
DROP TABLE genera;
-- +goose StatementEnd
