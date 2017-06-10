#include <Arduino.h>
#include <SimpleDHT.h>

#define PIN_DHT11 7

SimpleDHT11 dht11;
char buff[8];

void setup() { 
	Serial.begin(115200);	
}

void loop() { 
	delay(5000);
	byte temperature = 0;
	byte humidity = 0;
	if (dht11.read(PIN_DHT11, &temperature, &humidity, NULL)) {
		// error
	} else {
		sprintf(buff, "%2d", temperature);
		Serial.println(buff);
	}
}
