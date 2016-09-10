#define Serial SerialUSB

const int analogInPin = A0; 
const int calibTime = 15000;
boolean calibrating = true;
double threshold = 0;

void setup() {
  
  Serial.begin(9600);
}

void loop() {
  boolean hit = false;

  if(calibrating) {
  
    int counter = 0;
    int calibSum = 0;
    while(counter < calibTime) {
      calibSum += analogRead(analogInPin);
      counter ++;
      delay(1);
    }
  
    double mean = calibSum/calibTime;

    int calibVarianceSum = 0;
    for(int i=0; i < calibTime; i++) {
      int sig = analogRead(analogInPin);
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

  while(!hit) {
    int analogSig = analogRead(analogInPin);
    if (analogSig < threshold)
    {
      Serial.println ("Headshot");
      hit = true;
    }      
  }
//  int len = 100;
//  int analogSig[len];
//  for(int i = 0; i < len; i++) {
//    analogSig[i] = analogRead(analogInPin);
//    delay(1);
//  } 

//  int minsig = 99999;
//  for (int j = 0; j < len; j++) {
//    minsig = min(minsig, signal[j]);
//  }

//  Serial.print("Sensor Value: "); // gives the individual values for the sensor
//  Serial.println(minsig);
  delay(500);
}
