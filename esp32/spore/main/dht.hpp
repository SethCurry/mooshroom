#ifndef DHT_HPP
#define DHT_HPP

#define DHT_OK 0
#define DHT_CHECKSUM_ERROR -1
#define DHT_TIMEOUT_ERROR -2

#include "driver/gpio.h"



void errorHandler(int response);


struct DHTReading {
	float humidity;
	float temperature;
};

typedef struct DHTReading dht_reading_t;



class DHTClient {
  gpio_num_t gpio_pin;
  dht_reading_t reading;

  public:
    DHTClient(int pin);
    int read();
    float getHumidity();
    float getTemperature();
};

#endif