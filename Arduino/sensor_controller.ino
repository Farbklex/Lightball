#define Serial SerialUSB

#define PIN_COPPER_SENSOR 5
#define PIN_ANAGLOG_IN A0
#define PIN_OUTPUT 12

const int calibTime = 15000;
boolean calibrating = true;
double threshold = 0;

void setup() {
    Serial.begin(9600);
    
    pinMode(PIN_COPPER_SENSOR, INPUT);
    pinMode(PIN_OUTPUT, OUTPUT);
    digitalWrite(PIN_OUTPUT, LOW);
    
    pinMode(LED_BUILTIN, OUTPUT);
    digitalWrite(LED_BUILTIN, LOW);
    
    calibrate();
    digitalWrite(LED_BUILTIN, HIGH);
}

void calibrate() {
    int counter = 0;
    int calibSum = 0;
    while(counter < calibTime) {
      calibSum += analogRead(PIN_ANAGLOG_IN);
      counter ++;
      delay(1);
    }
    
    double mean = calibSum/calibTime;
    
    int calibVarianceSum = 0;
    for(int i=0; i < calibTime; i++) {
      int sig = analogRead(PIN_ANAGLOG_IN);
      calibVarianceSum += pow(mean - sig,2);
      delay(1);
    }
    double variance = calibVarianceSum/calibTime;
    threshold = mean - (1.7*sqrt(variance));
    
    calibrating = false;  
    Serial.print("Mean Sensor Value: ");
    Serial.println(mean); 
    Serial.print("Variance Sensor Value: ");
    Serial.println(variance); 
    Serial.print("Threshold Sensor Value: ");
    Serial.println(threshold); 
}

void loop() {
    // int analogSig = analogRead(PIN_ANAGLOG_IN);
    // if (analogSig < threshold) {
    //     sendHit();
    // }
    
    if (digitalRead(PIN_COPPER_SENSOR) == HIGH) {
        sendHit();
    }
}

void sendHit() {
    digitalWrite(PIN_OUTPUT, HIGH);
    delay(1000);
    digitalWrite(PIN_OUTPUT, LOW);
}
