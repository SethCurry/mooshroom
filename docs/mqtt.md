# MQTT

MQTT uses `+` for single-level wildcards, and `#` for multi-level wildcards.

## Topic Structure

Users can configure a prefix for mooshroom to use. The default is `mooshroom`.

Topics:

| Topic Format                    | Description                        |
| ------------------------------- | ---------------------------------- |
| `$prefix/spores/$name/dht_data` | Topic used for submitting DHT data |
