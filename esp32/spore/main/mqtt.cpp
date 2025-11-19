#include <stdio.h>
#include "esp_log.h"
#include "mqtt_client.h"
#include "sdkconfig.h"

class MQTTClient {
  esp_mqtt_client_handle_t client;

  public:
    MQTTClient(char *broker, char *username, char *password);
    void publish(char *topic, char *payload);
};

MQTTClient::MQTTClient(char *broker, char *username, char *password) {
  esp_mqtt_client_config_t config = {0};

  config.broker.address.uri = broker;
  config.credentials.username = username;
  config.credentials.authentication.password = password;

  client = esp_mqtt_client_init(&config);
  esp_mqtt_client_start(client);
}

void MQTTClient::publish(char *topic, char *payload) {
  esp_mqtt_client_publish(client, topic, payload, 0, 0, 0);
}