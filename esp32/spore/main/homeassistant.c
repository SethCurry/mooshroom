#include <stdio.h>
#include <string.h>
#include "homeassistant.h"
#include "mqtt_client.h"
#include "esp_log.h"

/*
{
  name: "Temperature",
  uniq_id: unique_id,
  stat_t: "topic name",
  val_tpl: "{{ value_json.temperature }}"
  unit_of_meas: "°C"
  dev_cla: "temperature"
  stat_cla: "measurement"
  avty_t: "availability topic"
  json_attr_t: "telemetry_topic"
  json_attr_tpl: "{{ value_json | tojson }}"
  dev: {
    "ids": device_id,
    "name": device_id,
    "sw": version,
    "mdl": model,
    mf: manufacturer
  }
}

{
  name: "Humidity",
  uniq_id: unique_id,
  stat_t: "topic name",
  val_tpl: "{{ value_json.humidity }}"
  unit_of_meas: "%"
  dev_cla: "humidity"
  stat_cla: "measurement"
  avty_t: "availability topic"
  json_attr_t: "telemetry_topic"
  json_attr_tpl: "{{ value_json | tojson }}"
  dev: {
    "ids": device_id,
    "name": device_id,
    "sw": version,
    "mdl": model,
    mf: manufacturer
  }
}


*/

int get_json_str_len(char *key, char *value) {
  int gotLen = snprintf(NULL, 0, "\"%s\":\"%s\"", key, value);
  return gotLen;
}

int get_json_float_len(char *key, float value) {
  int gotLen = snprintf(NULL, 0, "\"%s\":%.1f", key, value);
  return gotLen;
}

void append_json_str(char *buf, char *key, char *value, bool prependComma) {
  int gotLen = 0;

  if (prependComma) {
    gotLen = snprintf(NULL, 0, ",\"%s\":\"%s\"", key, value);
  } else {
    gotLen = snprintf(NULL, 0, "\"%s\":\"%s\"", key, value);
  }

  char newBuf[gotLen + 1];

  if (prependComma) {
    snprintf(newBuf, gotLen + 1, ",\"%s\":\"%s\"", key, value);
  } else {
    snprintf(newBuf, gotLen + 1, "\"%s\":\"%s\"", key, value);
  }

  strncat(buf, newBuf, gotLen);
}

int autodiscovery_config_json_len(ha_autodiscovery_config_t *config) {
  int numFields = 0;
  int totalLen = 3;
  bool hasDevFields = false;

  if (config->name != NULL) {
    numFields++;
    totalLen += get_json_str_len("name", config->name);
  }

  if (config->unique_id != NULL) {
    numFields++;
    totalLen += get_json_str_len("uniq_id", config->unique_id);
  }

  if (config->stat_topic != NULL) {
    numFields++;
    totalLen += get_json_str_len("stat_t", config->stat_topic);
  }

  if (config->value_template != NULL) {
    numFields++;
    totalLen += get_json_str_len("val_tpl", config->value_template);
  }

  if (config->unit_of_measurement != NULL) {
    numFields++;
    totalLen += get_json_str_len("unit_of_meas", config->unit_of_measurement);
  }

  if (config->device_class != NULL) {
    numFields++;
    totalLen += get_json_str_len("dev_cla", config->device_class);
  }

  if (config->stat_class != NULL) {
    numFields++;
    totalLen += get_json_str_len("stat_cla", config->stat_class);
  }

  if (config->availability_topic != NULL) {
    numFields++;
    totalLen += get_json_str_len("avty_t", config->availability_topic);
  }

  if (config->json_attribute_topic != NULL) {
    numFields++;
    totalLen += get_json_str_len("json_attr_t", config->json_attribute_topic);
  }

  if (config->json_attribute_template != NULL) {
    numFields++;
    totalLen += get_json_str_len("json_attr_tpl", config->json_attribute_template);
  }

  if (config->device_id != NULL) {
    hasDevFields = true;
    numFields++;
    totalLen += get_json_str_len("ids", config->device_id);
  }

  if (config->device_name != NULL) {
    hasDevFields = true;
    numFields++;
    totalLen += get_json_str_len("name", config->device_name);
  }

  if (config->device_version != NULL) {
    hasDevFields = true;
    numFields++;
    totalLen += get_json_str_len("sw", config->device_version);
  }

  if (config->device_model != NULL) {
    hasDevFields = true;
    numFields++;
    totalLen += get_json_str_len("mdl", config->device_model);
  }

  if (config->device_manufacturer != NULL) {
    hasDevFields = true;
    numFields++;
    totalLen += get_json_str_len("mf", config->device_manufacturer);
  }

  int numCommas = numFields - 1;

  if (hasDevFields) {
    totalLen += 8;
  }

  totalLen += numCommas;

  return totalLen;
};

void autodiscovery_config_to_json(char *jsonBuf, ha_autodiscovery_config_t *config) {
  bool wroteField = false;

  strncat(jsonBuf, "{", 2);
  if (config->name != NULL)
  {
    append_json_str(jsonBuf, "name", config->name, wroteField);
    wroteField = true;
  }

  if (config->unique_id != NULL) {
    append_json_str(jsonBuf, "uniq_id", config->unique_id, wroteField);
    wroteField = true;
  }

  if (config->stat_topic != NULL) {
    append_json_str(jsonBuf, "stat_t", config->stat_topic, wroteField);
    wroteField = true;
  }

  if (config->value_template != NULL) {
    append_json_str(jsonBuf, "val_tpl", config->value_template, wroteField);
    wroteField = true;
  }

  if (config->unit_of_measurement != NULL) {
    append_json_str(jsonBuf, "unit_of_meas", config->unit_of_measurement, wroteField);
    wroteField = true;
  }

  if (config->device_class != NULL) {
    append_json_str(jsonBuf, "dev_cla", config->device_class, wroteField);
    wroteField = true;
  }

  if (config->stat_class != NULL) {
    append_json_str(jsonBuf, "stat_cla", config->stat_class, wroteField);
    wroteField = true;
  }

  if (config->availability_topic != NULL) {
    append_json_str(jsonBuf, "avty_t", config->availability_topic, wroteField);
    wroteField = true;
  }

  if (config->json_attribute_topic != NULL) {
    append_json_str(jsonBuf, "json_attr_t", config->json_attribute_topic, wroteField);
    wroteField = true;
  }

  if (config->json_attribute_template != NULL) {
    append_json_str(jsonBuf, "json_attr_tpl", config->json_attribute_template, wroteField);
    wroteField = true;
  }

  if (config->device_id != NULL || config->device_name != NULL || config->device_version != NULL || config->device_model != NULL || config->device_manufacturer != NULL) {
    bool hasDevComma = false;
    strncat(jsonBuf, ",\"dev\":{", 9);

    if (config->device_id != NULL) {
      append_json_str(jsonBuf, "ids", config->device_id, hasDevComma);
      hasDevComma = true;
    }

    if (config->device_name != NULL) {
      append_json_str(jsonBuf, "name", config->device_name, hasDevComma);
      hasDevComma = true;
    }

    if (config->device_version != NULL) {
      append_json_str(jsonBuf, "sw", config->device_version, hasDevComma);
      hasDevComma = true;
    }

    if (config->device_model != NULL) {
      append_json_str(jsonBuf, "mdl", config->device_model, hasDevComma);
      hasDevComma = true;
    }

    if (config->device_manufacturer != NULL) {
      append_json_str(jsonBuf, "mf", config->device_manufacturer, hasDevComma);
      hasDevComma = true;
    }

    strncat(jsonBuf, "}", 2);
  }

  strncat(jsonBuf, "}", 2);
};

char * ha_autodiscovery_mqtt_config_json(ha_autodiscovery_config_t *config) {
  int jsonLen = autodiscovery_config_json_len(config);

  char *jsonBuf;
  jsonBuf = calloc(jsonLen, sizeof(char));

  autodiscovery_config_to_json(jsonBuf, config);

  return jsonBuf;
}

void send_ha_config(esp_mqtt_client_handle_t client, char *config_topic, ha_autodiscovery_config_t *config) {
  char *config_json = ha_autodiscovery_mqtt_config_json(config);

  esp_mqtt_client_publish(client, config_topic, config_json, 0, 0, 0);
  free(config_json);
}

void send_ha_temperature_dht_config(esp_mqtt_client_handle_t client, char* const device_id, char* const topic) {
  int tempLen = snprintf(NULL, 0, "{\"name\":\"%s Temperature\",\"uniq_id\":\"%s-temperature\",\"stat_t\":\"%s\",\"val_tpl\":\"{{value_json.temperature}}\",\"unit_of_meas\":\"C\",\"dev_cla\":\"temperature\",\"stat_cla\":\"measurement\"}", device_id, device_id, topic);

  char tempBuf[tempLen+1];

  snprintf(tempBuf, tempLen+1, "{\"name\":\"%s Temperature\",\"uniq_id\":\"%s-temperature\",\"stat_t\":\"%s\",\"val_tpl\":\"{{value_json.temperature}}\",\"unit_of_meas\":\"C\",\"dev_cla\":\"temperature\",\"stat_cla\":\"measurement\"}", device_id, device_id, topic);

  int topicLen = snprintf(NULL, 0, "homeassistant/sensor/%s-temperature/config", device_id);
  char topicBuf[topicLen+1];

  snprintf(topicBuf, topicLen+1, "homeassistant/sensor/%s-temperature/config", device_id);

  esp_mqtt_client_publish(client, topicBuf, tempBuf, 0, 0, 0);
}

void send_ha_humidity_dht_config(esp_mqtt_client_handle_t client, char* const device_id, char* const topic) {
  int humidLen = snprintf(NULL, 0, "{\"name\":\"%s Humidity\",\"uniq_id\":\"%s-humidity\",\"stat_t\":\"%s\",\"val_tpl\":\"{{value_json.humidity}}\",\"unit_of_meas\":\"%%\",\"dev_cla\":\"humidity\",\"stat_cla\":\"measurement\"}", device_id, device_id, topic);

  char humidBuf[humidLen+1];

  snprintf(humidBuf, humidLen+1, "{\"name\":\"%s Humidity\",\"uniq_id\":\"%s-humidity\",\"stat_t\":\"%s\",\"val_tpl\":\"{{value_json.humidity}}\",\"unit_of_meas\":\"%%\",\"dev_cla\":\"humidity\",\"stat_cla\":\"measurement\"}", device_id, device_id, topic);

  int topicLen = snprintf(NULL, 0, "homeassistant/sensor/%s-humidity/config", device_id);
  char topicBuf[topicLen+1];

  snprintf(topicBuf, topicLen+1, "homeassistant/sensor/%s-humidity/config", device_id);

  esp_mqtt_client_publish(client, topicBuf, humidBuf, 0, 0, 0);
}

void send_ha_dht_config(esp_mqtt_client_handle_t client, char* const device_id, char* const topic) {
  send_ha_temperature_dht_config(client, device_id, topic);
  send_ha_humidity_dht_config(client, device_id, topic);
}