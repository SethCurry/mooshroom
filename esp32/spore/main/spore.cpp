#include <stdio.h>
#include "mqtt.hpp"
#include "wifi.hpp"
#include "nvs_flash.h"

extern "C" void app_main(void)
{
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
      ESP_ERROR_CHECK(nvs_flash_erase());
      ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);

    start_wifi();

    char mqttBrokerURL[] = "mqtt://10.0.0.56:1883";
    char mqttUsername[] = "esp";
    char mqttPassword[] = "esp";

    MQTTClient mqttClient(mqttBrokerURL, mqttUsername, mqttPassword);

    char mqttTopic[] = "test/topic";
    char mqttMessage[] = "Hello, World!";
    mqttClient.publish(mqttTopic, mqttMessage);
}
