#include "Arduino.h"
#include "Servo.h"

// Declare the Servo pin 
int servoPin = 2; 
// Create a servo object 
Servo Servo1; 




void setup()
{
//    pinMode(motorPin, OUTPUT);
    initBluetooth();
    initController();
    
//	pinMode(LED_BUILTIN, OUTPUT);
//    Servo1.attach(servoPin); 
}

int motorSpeed = 0;
void loop()
{
    readCommand();
//    loopServo();
// updateSerial();
//    readSerial();
    /*
    motorSpeed += 10;
    if(motorSpeed > 250)
        motorSpeed = 0;
    analogWrite(motorPin, motorSpeed);
    */
    /*
    analogWrite(bled, 250);
    delay(50);
    analogWrite(bled, 0);
    delay(3000);
    */
   //delay(500);
}

void loopServo() {
       // Make servo go to 0 degrees 
   Servo1.write(0); 
   delay(1000); 
   // Make servo go to 90 degrees 
   Servo1.write(90); 
   delay(5000); 
   // Make servo go to 180 degrees 
   Servo1.write(180); 
   delay(1000); 

   Servo1.write(90); 
   delay(5000); 
    /*
  digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
  delay(100);                       // wait for a second
  digitalWrite(LED_BUILTIN, LOW);    // turn the LED off by making the voltage LOW
  delay(1000);                       // wait for a second
  */
}