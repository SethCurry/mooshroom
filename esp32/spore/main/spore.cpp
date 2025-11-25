#include <stdio.h>
#include "mqtt.hpp"
#include "wifi.hpp"
#include "dht.hpp"
#include "nvs_flash.h"
#include "freertos/task.h"
#include "homeassistant.hpp"

void register_with_ha(esp_mqtt_client_handle_t mqtt_conn) {
  ha_autodiscovery_config_t ha_temperature_cfg = {
    .name = "office Temperature",
    .unique_id = "office-temperature",
    .stat_topic = "office/climate",
    .value_template = "{{value_json.temperature}}",
    .unit_of_measurement = "C",
    .device_class = "temperature",
    .stat_class = "measurement",
    .device_id = "esp-climate",
  };


  send_ha_config(mqtt_conn, "homeassistant/sensor/office-temperature/config", &ha_temperature_cfg);

  ha_autodiscovery_config_t ha_humidity_cfg = {
    .name = "office Humidity",
    .unique_id = "office-humidity",
    .stat_topic = "office/climate",
    .value_template = "{{value_json.humidity}}",
    .unit_of_measurement = "%",
    .device_class = "humidity",
    .stat_class = "measurement",
    .device_id = "esp-climate",
  };
  send_ha_config(mqtt_conn, "homeassistant/sensor/office-temperature/config", &ha_humidity_cfg);
}

extern "C" void app_main(void)
{
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
      ESP_ERROR_CHECK(nvs_flash_erase());
      ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);

    start_wifi();

    char mqttBrokerURL[] = CONFIG_MQTT_URL;
    char mqttUsername[] = CONFIG_MQTT_USERNAME;
    char mqttPassword[] = CONFIG_MQTT_PASSWORD;

    MQTTClient mqttClient(mqttBrokerURL, mqttUsername, mqttPassword);

    //register_with_ha(mqttClient.client);

    int mqttTopicLen = snprintf(NULL, 0, "%s/spores/%s/dht_data", CONFIG_SPORE_MQTT_PREFIX, CONFIG_SPORE_NAME);

    char mqttTopic[mqttTopicLen + 1];

    snprintf(mqttTopic, mqttTopicLen + 1, "%s/spores/%s/dht_data", CONFIG_SPORE_MQTT_PREFIX, CONFIG_SPORE_NAME);

    DHTClient dhtClient(CONFIG_DHT_PIN);

    while (1) {
        int ret = dhtClient.read();

        if (ret != DHT_OK) {
          vTaskDelay(1000 / portTICK_PERIOD_MS);
          continue;
        }

        char *readingJson = dhtClient.json();

        printf("publishing message to %s\n", mqttTopic);
        printf("%s\n", readingJson);
        mqttClient.publish(mqttTopic, readingJson);

        free(readingJson);
        vTaskDelay((CONFIG_DHT_PUBLISH_INTERVAL_SECONDS * 1000) / portTICK_PERIOD_MS);
    }
}
