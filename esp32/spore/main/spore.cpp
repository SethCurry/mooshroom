#include <stdio.h>
#include "mqtt.hpp"
#include "wifi.hpp"
#include "dht.hpp"
#include "nvs_flash.h"
#include "freertos/task.h"

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

    DHTClient dhtClient(15);

    while (1) {
        int ret = dhtClient.read();
        errorHandler(ret);

        char *readingJson = dhtClient.json();

        /*
        float humidity = dhtClient.getHumidity();
        float temperature = dhtClient.getTemperature();

        printf("Humidity %.1f%%\n", humidity);
        printf("Temperature %.1fC\n", temperature);
        */
        printf("%s\n", readingJson);
        mqttClient.publish(mqttTopic, readingJson);
        free(readingJson);
        vTaskDelay(1000 / portTICK_PERIOD_MS);
    }
}
