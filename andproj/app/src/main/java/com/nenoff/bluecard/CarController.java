package com.nenoff.bluecard;

public class CarController {
    private static byte LED_COMMAND = 0x0;
    private static byte STEER = 0x1;

    private static byte VALUE_OFF = 0x0;
    private static byte VALUE_ON = (byte)0xFF;

    private static byte END = 0x0A;

    private BLEController bleController;

    public CarController(BLEController bleController) {
        this.bleController = bleController;
    }

    private byte [] createControllWord(byte command, byte value) {
        return new byte[]{command, value, END};
    }

    private byte [] createControllWord(byte command, byte value1, byte value2) {
        return new byte[]{command, value1, value2, END};
    }

    public void switchLED(boolean on) {
        this.bleController.sendData(createControllWord(LED_COMMAND, on?VALUE_ON:VALUE_OFF));
    }

    public void steer(byte lr, byte fb) {
        this.bleController.sendData(createControllWord(STEER, lr, fb));
    }
}
