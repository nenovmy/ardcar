#include "Arduino.h"
#include "Servo.h"

#include "bluetooth.h"

// Declare the Servo pin 
int servoPin = 2; 
// Create a servo object 
Servo Servo1; 

const int bled = 7; 		//initializing pin 2 as ‘pwm’ variable

void setup()
{
    initBluetooth();
    sendCommand("AT");
    sendCommand("AT+ROLE0");
    sendCommand("AT+UUID0xFFE0");
    sendCommand("AT+CHAR0xFFE1");
    sendCommand("AT+NAMEBlueCar");
    pinMode(bled, OUTPUT);
//	pinMode(LED_BUILTIN, OUTPUT);
//    Servo1.attach(servoPin); 
}

void loop()
{
    readSerial();
    delay(500);
    /*
    analogWrite(bled, 250);
    delay(50);
    analogWrite(bled, 0);
    delay(3000);
    */
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