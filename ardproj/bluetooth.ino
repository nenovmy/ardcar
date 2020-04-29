#include "Arduino.h"
#include "SoftwareSerial.h"
#include "HardwareSerial.h"

SoftwareSerial mySerial(2, 3); // TX, RX

const int blstat = 4;

int buff[10];
int currCh = 0;
bool isConnected = false;

long connChecktime = millis();

void initBluetooth() {
  pinMode(blstat, INPUT);
  mySerial.begin(9600);
  Serial.begin(9600);

  sendCommand("AT");
  sendCommand("AT+ROLE0");
  sendCommand("AT+UUIDFFE0");
  sendCommand("AT+CHARFFE1");
  sendCommand("AT+NAMEBlueCArd");
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

// Used for manual setup
void updateSerial() {
  if (mySerial.available())     	
   Serial.write(mySerial.read()); 

  if (Serial.available())   	
    mySerial.write(Serial.read());
}

void checkConnected() {
  long diff = millis() - connChecktime;
  if(diff < 300)
    return;
  connChecktime = millis();

  bool stat = digitalRead(blstat);

  if(stat) {
    if(!isConnected) {
      Serial.println("Device connected");
    }
    isConnected = true;
  }else {
    if(isConnected) {
      Serial.println("Device disconnected");
      shutDown();
    }
    isConnected = false;
  }
}

void readCommand() {
  checkConnected();

  while(mySerial.available()) {
    int nc = mySerial.read();
    if(nc == 10) {
      executeCommand(buff);
      currCh = 0;
    }else {
      buff[currCh++] = nc;
    }
  }
}
