#ifndef MQTT_HPP
#define MQTT_HPP

#include "mqtt_client.h"

class MQTTClient {
  esp_mqtt_client_handle_t client;

  public:
    MQTTClient(char *broker, char *username, char *password);
    void publish(char *topic, char *payload);
};
#endif