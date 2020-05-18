#include "Arduino.h"
#include "SoftwareSerial.h"
#include "HardwareSerial.h"

const int START_COMMAND = 0x1;

SoftwareSerial mySerial(2, 3); // TX, RX
int buff[10];
int currCh = 0;
bool isConnected = false;

long connChecktime = millis();
long heartbeatTime = 0l;

void initBluetooth() {
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

void heartbeat() {
  heartbeatTime = millis();
}

void checkConnected() {
  bool state = (millis() - heartbeatTime) < 2000;

  if (isConnected != state)
    connectionStateChanged(state);

  isConnected = state;
}

void connectionStateChanged(bool newState) {
  Serial.println(newState ? "Device connected" : "Device disconnected");

  if(!newState)
    shutDown();
}

void readCommand() {
  checkConnected();

  while (mySerial.available()) {
    int nc = mySerial.read();
    if (currCh > 0 || nc == START_COMMAND)
      parseNext(nc);
  }
}

void parseNext(int next) {
  buff[currCh++] = next;
  if (isCommandFullyRead()) {
    executeCommand(buff);
    currCh = 0;
  }
}

bool isCommandFullyRead() {
  return currCh > 2             && // command is at leas 3 bytes long (START, Command type, num of parameters)
         buff[2] == currCh - 3;    // are all parameters already read
}
