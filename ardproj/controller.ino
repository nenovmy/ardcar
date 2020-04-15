const int SWITCH_LED = 0x0;
const int STEER = 0x1;

const int bled = 7; 		//initializing pin 2 as ‘pwm’ variable
const int motorPin = 9;

void initController() {
    pinMode(bled, OUTPUT);
    pinMode(motorPin, OUTPUT);
}

void executeCommand(int cmd[]) {
  switch(cmd[0]) {
    case 0: switchLight(cmd[1]); break;
    case 1: steer(cmd[1], cmd[2]); break;
  }
}

void switchLight(int v) {
  if(v == 0) {
    Serial.println("Switch the linght off");
  }else {
    Serial.println("Switch the linght on");
  }
  switchLED(v);
}

void steer(int lr, int fb) {
    if(fb > 128) {
        int pow = (fb - 128) * 2;
        
        Serial.print("pow: ");
        Serial.println(pow);
        analogWrite(motorPin, pow);
    }else
    {
        analogWrite(motorPin, 0);
    }
    
}

void switchLED(int value) {
    analogWrite(bled, value);
}

void shutDown() {
  switchLight(0);
}