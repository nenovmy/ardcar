/*
 * (c) Matey Nenov (https://www.thinker-talk.com)
 * 
 * Licensed under Creative Commons: By Attribution 3.0
 * http://creativecommons.org/licenses/by/3.0/
 * 
 */

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
