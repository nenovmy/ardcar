#include "Arduino.h"

void setup()
{
    initBluetooth();
    initController();
}

void loop()
{
    readCommand();
}
