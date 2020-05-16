package com.nenoff.bluecard;

public class CarController {
    private static byte HEARTBEAT = 0x0;
    private static byte STEER = 0x1;
    private static byte LED_COMMAND = 0x2;

    private static byte VALUE_OFF = 0x0;
    private static byte VALUE_ON = (byte)0xFF;

    private static byte END = 0x0A;

    private BLEController bleController;

    public CarController(BLEController bleController) {
        this.bleController = bleController;
    }

    private byte [] createControllWord(byte type, byte ... args) {
        byte [] command = new byte[args.length + 2];
        command[0] = type;
        command[command.length - 1] = END;
        for(int i=0; i<args.length; i++)
            command[i+1] = args[i];

        return command;
    }

    public void switchLED(boolean on) {
        this.bleController.sendData(createControllWord(LED_COMMAND, on?VALUE_ON:VALUE_OFF));
    }

    public void steer(byte lr, byte fb) {
        this.bleController.sendData(createControllWord(STEER, lr, fb));
    }

    public void heartbeat() {
        this.bleController.sendData(createControllWord(HEARTBEAT));
    }
}
