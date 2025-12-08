-- +goose Up
-- +goose StatementBegin
ALTER TABLE enclosures ADD CONSTRAINT unique_enclosure_name UNIQUE (name);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP CONSTRAINT unique_enclosure_name;
-- +goose StatementEnd
