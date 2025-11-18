package mooshroom

import (
	"fmt"
	"io"
	"os"

	"github.com/goccy/go-yaml"
)

// MQTTConfig stores the configuration options for the MQTT client.
type MQTTConfig struct {
	Brokers []string `yaml:"brokers"`
}

// Config stores the configuration options for mooshroom.
type Config struct {
	MQTT MQTTConfig `yaml:"mqtt"`
}

func ParseConfig(reader io.Reader) (*Config, error) {
	var config Config
	if err := yaml.NewDecoder(reader).Decode(&config); err != nil {
		return nil, fmt.Errorf("failed to parse config: %w", err)
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
