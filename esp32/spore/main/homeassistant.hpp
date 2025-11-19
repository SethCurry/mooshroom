#ifndef HOMEASSISTANT_H
#define HOMEASSISTANT_H

#include "mqtt_client.h"

struct ha_autodiscovery_config {
  char *name;
  char *unique_id;
  char *stat_topic;
  char *value_template;
  char *unit_of_measurement;
  char *device_class;
  char *stat_class;
  char *availability_topic;
  char *json_attribute_topic;
  char *json_attribute_template;
  char *device_id;
  char *device_name;
  char *device_version;
  char *device_model;
  char *device_manufacturer;
};

typedef struct ha_autodiscovery_config ha_autodiscovery_config_t;


void send_ha_dht_config(esp_mqtt_client_handle_t client, char *const device_id, char *const topic);

char * ha_autodiscovery_mqtt_config_json(ha_autodiscovery_config_t *config);
void send_ha_config(esp_mqtt_client_handle_t client, char *config_topic, ha_autodiscovery_config_t *config);
#endif