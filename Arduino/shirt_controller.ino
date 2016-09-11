#include <Adafruit_NeoPixel.h>

#include <Adafruit_BLE.h>
#include <Adafruit_BluefruitLE_SPI.h>
#include <Adafruit_BluefruitLE_UART.h>
#include "BluefruitConfig.h"
#define Serial SerialUSB

// LED
#define LED_STRIP1 6
#define LED_STRIP2 9
#define NUM_PIXELS 20
#define NUM_TEAM_PIXELS 2

// Other pins
#define PIN_TRIGGER 5

// Bluetooth
#define BLUEFRUIT_SPI_CS 8
#define BLUEFRUIT_SPI_IRQ 7
#define BLUEFRUIT_SPI_RST 4
#define FACTORYRESET_ENABLE 1
// #define MODE_LED_BEHAVIOUR "BLUEART"

int health = 100;

// Config
// #define BT_NAME "Lightball Gear Alpha"

Adafruit_NeoPixel strip = Adafruit_NeoPixel(NUM_PIXELS + NUM_TEAM_PIXELS, LED_STRIP1, NEO_GRB);
Adafruit_NeoPixel strip2 = Adafruit_NeoPixel(NUM_PIXELS + NUM_TEAM_PIXELS, LED_STRIP2, NEO_GRB);

Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

// A small helper
void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}

bool getUserInput(char buffer[], uint8_t maxSize)
{
  // timeout in 100 milliseconds
  TimeoutTimer timeout(100);

  memset(buffer, 0, maxSize);
  while( (!Serial.available()) && !timeout.expired() ) { delay(1); }

  if ( timeout.expired() ) return false;

  delay(2);
  uint8_t count=0;
  do
  {
    count += Serial.readBytes(buffer+count, maxSize);
    delay(2);
  } while( (count < maxSize) && (Serial.available()) );

  return true;
}

void setup() {
    pinMode(LED_BUILTIN, OUTPUT);
    // put your setup code here, to run once:
    Serial.begin(9600);
    
    pinMode(PIN_TRIGGER, INPUT);
    
    strip.begin();
    strip.show(); // Initialize all pixels to 'off'

    strip2.begin();
    strip2.show(); // Initialize all pixels to 'off'

    /* Initialise the module */
    Serial.print("Initialising the Bluefruit LE module: ");

    if (!ble.begin(VERBOSE_MODE) )
    {
        error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
    }
    Serial.println("DONE!");

    if ( FACTORYRESET_ENABLE )
    {
        /* Perform a factory reset to make sure everything is in a known state */
        Serial.println("Performing a factory reset: ");
        if (!ble.factoryReset()) {
          error(F("Couldn't factory reset"));
        }
    }

    /* Change the device name to make it easier to find */
    Serial.println(F("Setting device name"));
    if (! ble.sendCommandCheckOK(F( "AT+GAPDEVNAME=Lightball Gear Alpha" )) ) {
    error(F("Could not set device name?"));
    }

    // setStripCol(255, 255, 255);
    setStripCol(0, 0, 0);

  Serial.println(F("Performing a SW reset (service changes require a reset): "));
  if (! ble.reset() ) {
    error(F("Couldn't reset??"));
  }
  
    // Serial.println(F("******************************"));
    // Serial.println(F("Change LED activity to " MODE_LED_BEHAVIOUR));
    // ble.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
    // Serial.println(F("******************************"));
  
  /* Disable command echo from Bluefruit */
  ble.verbose(false);
  ble.echo(false);
  
//   setTeam(255,255,255);
setTeam(0, 0, 0);
  
  /* Wait for connection */
  while (! ble.isConnected()) {
      delay(500);
  }
  
  
  setStripCol(0, 0, 0);
  strip.show();
  strip2.show();
  delay(300);
  setStripCol(0, 0, 255);
  strip.show();
  strip2.show();
  delay(300);
  setStripCol(0, 0, 0);
  strip.show();
  strip2.show();
  delay(300);
  setStripCol(0, 0, 255);
  strip.show();
  strip2.show();
  delay(300);
  setStripCol(0, 0, 0);
  strip.show();
  strip2.show();
  delay(300);
  setStripCol(0, 0, 255);
  strip.show();
  strip2.show();
  delay(300);
  setStripCol(0, 0, 0);
  strip.show();
  strip2.show();
  delay(300);
  setStripCol(255, 255, 255);
  strip.show();
  strip2.show();

  Serial.println("Requesting Bluefruit info:");
  /* Print Bluefruit information */
  ble.info();

  delay(5000);
}

void loop() {
  // Check for user input
  char inputs[BUFSIZE+1];
  
  // Hit trigger
  if (digitalRead(PIN_TRIGGER) == HIGH) {
    health = health - 25;
    if (health < 0) {
        health = 100;
    }
    setHealth(health);
    delay(1000);
  }

  if ( getUserInput(inputs, BUFSIZE) )
  {
    // Send characters to Bluefruit
    Serial.print("[Send] ");
    Serial.println(inputs);

    ble.print("AT+BLEUARTTX=");
    ble.println(inputs);

    // check response stastus
    if (! ble.waitForOK() ) {
      Serial.println(F("Failed to send?"));
    }
  }

  // Check for incoming characters from Bluefruit
  ble.println("AT+BLEUARTRX");
  ble.readline();
  if (strcmp(ble.buffer, "OK") == 0) {
    // no data
  } else {
    parseBtCommand();
    ble.waitForOK();
  }
  // Some data was found, its in the buffer
//   Serial.print(F("[Recv] ")); Serial.println(ble.buffer);
  
}


// Communication protocol
bool parseBtCommand() {
    char code = ble.buffer[0];
    
    if (code == '1') {
        // Set health
        Serial.println("[recv 1]");
        uint8_t val = ble.buffer[1];
        health = val;
        setHealth(val);
    } else if (code == '2') {
        // Set team
        Serial.println("[recv 2]");
        char team = ble.buffer[1];
        
        if (ble.buffer[1] == '1') {
            setTeam(0, 0, 255);
        } else if (ble.buffer[1] == '2') {
            setTeam(255, 204, 0);
        }
    } else if (code == '3') {
        // Reset
        setStripCol(255, 255, 255);
        setTeam(255, 255, 255);
    } else {
        return false;
    }
    
    return true;
}

// LED strip methods
void rainbow(uint8_t wait) {
  uint16_t i, j;

  for(j=0; j<256; j++) {
    for(i=0; i<strip.numPixels(); i++) {
      strip.setPixelColor(i, Wheel((i+j) & 255));
      strip2.setPixelColor(i, Wheel((i+j) & 255));
    }
    strip.show();
    strip2.show();
    delay(wait);
  }
}

// Input a value 0 to 255 to get a color value.
// The colours are a transition r - g - b - back to r.
uint32_t Wheel(byte WheelPos) {
  WheelPos = 255 - WheelPos;
  if(WheelPos < 85) {
    return strip.Color(255 - WheelPos * 3, 0, WheelPos * 3);
    return strip2.Color(255 - WheelPos * 3, 0, WheelPos * 3);
  }
  if(WheelPos < 170) {
    WheelPos -= 85;
    return strip.Color(0, WheelPos * 3, 255 - WheelPos * 3);
    return strip2.Color(0, WheelPos * 3, 255 - WheelPos * 3);
  }
  WheelPos -= 170;
  return strip.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
  return strip2.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
}

// Helpers
void setCol(uint8_t pixel, uint8_t r, uint8_t g, uint8_t b) {
    strip.setPixelColor(pixel, r, g, b);
    strip2.setPixelColor(pixel, r, g, b);
}

void setStripCol(uint8_t r, uint8_t g, uint8_t b) {
    for (int i = 0; i < NUM_PIXELS; i++) {
        setCol(i, r, g, b);
    }
    strip.show();
    strip2.show();
}

void setHealth(uint16_t health) {
  uint16_t pixelsFull = (float) NUM_PIXELS * ((float)health / 100);
  uint16_t pixelsEmpty = NUM_PIXELS - pixelsFull;
  
  uint16_t i, j;

  for (i = 0; i < pixelsFull; i++) {
    setCol(i, 0, 255, 0);
  }
  for (j = pixelsFull; j < NUM_PIXELS; j++) {
    setCol(j, 255, 0, 0);
  }
  strip.show();
  strip2.show();
  
  if (health == 0) {
    for (j = 0; j < NUM_PIXELS; j++) {
        setCol(j, 0, 0, 0);
    }
    strip.show();
    strip2.show();
    delay(200);
    for (j = 0; j < NUM_PIXELS; j++) {
        setCol(j, 255, 0, 0);
    }
    strip.show();
    strip2.show();
    delay(200);
    for (j = 0; j < NUM_PIXELS; j++) {
        setCol(j, 0, 0, 0);
    }
    strip.show();
    strip2.show();
    delay(200);
    for (j = 0; j < NUM_PIXELS; j++) {
        setCol(j, 255, 0, 0);
    }
    strip.show();
    strip2.show();
    for (j = 0; j < NUM_PIXELS; j++) {
        setCol(j, 0, 0, 0);
    }
    strip.show();
    strip2.show();
    delay(200);
    for (j = 0; j < NUM_PIXELS; j++) {
        setCol(j, 255, 0, 0);
    }
    strip.show();
    strip2.show();
    for (j = 0; j < NUM_PIXELS; j++) {
        setCol(j, 0, 0, 0);
    }
    strip.show();
    strip2.show();
    delay(200);
    for (j = 0; j < NUM_PIXELS; j++) {
        setCol(j, 255, 0, 0);
    }
    strip.show();
    strip2.show();
  }
}

void setTeam(uint8_t r, uint8_t g, uint8_t b) {
    for (int i = NUM_PIXELS; i < NUM_PIXELS + NUM_TEAM_PIXELS; i++) {
        Serial.println("setCol(i, r, g, b);");
        setCol(i, r, g, b);
    }
    strip.show();
    strip2.show();
}