#include "Servo.h"

const int SWITCH_LED = 0x0;
const int STEER = 0x1;

const int lights = 12;  //initializing pin 12 as car lights
const int servoPin = 9; // steering

const int L9110_IA = 5; // Pin D10 --> Motor Input A
const int L9110_IB = 6; // Pin D11 --> Motor Input B
 
// Motor Speed & Direction
const int motorPWM = L9110_IA; // Motor PWM Speed
const int motorDir = L9110_IB; // Motor Direction

Servo servo; 


void initController() {
  pinMode(lights, OUTPUT);
  pinMode( motorDir, OUTPUT );
  pinMode( motorPWM, OUTPUT );
  servo.attach(servoPin); 
  stopMottors();
}

void executeCommand(int cmd[]) {
  switch(cmd[0]) {
    case SWITCH_LED: switchLight(cmd[1]); break;
    case STEER: carControl(cmd[1], cmd[2]); break;
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

void carControl(int lr, int fb) {
      steer(lr);
      motorPower(fb);
}

void steer(int lr) {
  servo.write(lr);
}

void motorPower(int fb) {
    if(fb == 128)
      stopMottors();
    else if(fb > 128)
      moveForward(fb);
    else
      moveBackwards(fb);
}

void stopMottors() {
  digitalWrite( motorDir, LOW ); // Set motor to off
  digitalWrite( motorPWM, LOW );
}

void moveForward(int fb) {
  digitalWrite( motorDir, HIGH ); // direction = forward
  int pow = 255 - ((fb - 128) * 2);
  analogWrite( motorPWM, pow );
}

void moveBackwards(int fb) {
  digitalWrite( motorDir, LOW ); // direction = backwards
  int pow = 255 - (fb * 2);
  analogWrite(motorPWM, pow);
}

void switchLED(int value) {
    analogWrite(lights, value);
}

void shutDown() {
  switchLED(0);
  stopMottors();
  servo.write(90);
}