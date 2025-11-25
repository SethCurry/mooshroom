package mooshroom

import (
	"fmt"
	"io"
	"os"

	"github.com/SethCurry/mooshroom/internal/validate"
	"github.com/goccy/go-yaml"
)

type SQLConfig struct {
	URL string `yaml:"url"`
}

func (s *SQLConfig) Validate() []*validate.ValidationError {
	errors := make([]*validate.ValidationError, 0)
	if s.URL == "" {
		errors = append(errors, validate.NewValidationError("url", "is required"))
	}
	return errors
}

type HTTPConfig struct {
	Port int `yaml:"port"`
}

func (h *HTTPConfig) Validate() []*validate.ValidationError {
	errors := make([]*validate.ValidationError, 0)
	if h.Port == 0 {
		errors = append(errors, validate.NewValidationError("port", "is required"))
	}
	return errors
}

// MQTTConfig stores the configuration options for the MQTT client.
type MQTTConfig struct {
	Brokers  []string `yaml:"brokers"`
	Username string   `yaml:"username"`
	Password string   `yaml:"password"`
	Prefix   string   `yaml:"prefix"`
}

func (m *MQTTConfig) Validate() []*validate.ValidationError {
	errors := make([]*validate.ValidationError, 0)
	if len(m.Brokers) == 0 {
		errors = append(errors, validate.NewValidationError("brokers", "is required"))
	}
	if m.Prefix == "" {
		errors = append(errors, validate.NewValidationError("prefix", "is required"))
	}
	if m.Username == "" {
		errors = append(errors, validate.NewValidationError("username", "is required"))
	}
	if m.Password == "" {
		errors = append(errors, validate.NewValidationError("password", "is required"))
	}
	return errors
}

// Config stores the configuration options for mooshroom.
type Config struct {
	MQTT MQTTConfig `yaml:"mqtt"`
	HTTP HTTPConfig `yaml:"http"`
	SQL  SQLConfig  `yaml:"sql"`
}

func (c *Config) Validate() []*validate.ValidationError {
	errors := make([]*validate.ValidationError, 0)
	errors = append(errors, c.MQTT.Validate()...)
	errors = append(errors, c.HTTP.Validate()...)
	errors = append(errors, c.SQL.Validate()...)

	return errors
}

func ParseConfig(reader io.Reader) (*Config, error) {
	var config Config
	if err := yaml.NewDecoder(reader).Decode(&config); err != nil {
		return nil, fmt.Errorf("failed to parse config: %w", err)
	}

	validationError := validate.Validate(&config)
	if validationError != nil {
		return nil, validationError
	}
	return &config, nil
}

func ParseConfigFile(path string) (*Config, error) {
	reader, err := os.Open(path)
	if err != nil {
		return nil, fmt.Errorf("failed to open config file: %w", err)
	}
	defer reader.Close()
	return ParseConfig(reader)
}
