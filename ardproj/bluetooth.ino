#include "Arduino.h"
#include "SoftwareSerial.h"
#include "HardwareSerial.h"

SoftwareSerial mySerial(5, 4); // TX, RX

void initBluetooth() {
    mySerial.begin(9600);
    Serial.begin(9600);
}

void sendCommand(const char * command) {
  Serial.print("Command send :");
  Serial.println(command);
  mySerial.println(command);
  //wait some time
  delay(100);

  char reply[100];
  int i = 0;
  while (mySerial.available()) {
    reply[i] = mySerial.read();
    i += 1;
  }
  //end the string
  reply[i] = '\0';
  Serial.print(reply);
  Serial.println("\nReply end");                 
  delay(100);
}


void readSerial(){
  char reply[50];
  int i = 0;
  while (mySerial.available()) {
    reply[i] = mySerial.read();
    i += 1;
  }
  //end the string
  reply[i] = '\0';
  if(strlen(reply) > 0){
    Serial.print(reply);
    Serial.println("We have just read some data");
  }
}