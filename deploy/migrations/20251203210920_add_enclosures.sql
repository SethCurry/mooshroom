-- +goose Up
-- +goose StatementBegin
CREATE TABLE enclosures (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

ALTER TABLE spores ADD COLUMN enclosure_id INTEGER REFERENCES enclosures(id);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
ALTER TABLE spores DROP COLUMN enclosure_id;
DROP TABLE enclosures;
-- +goose StatementEnd
