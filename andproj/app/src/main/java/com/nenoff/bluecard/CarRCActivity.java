package com.nenoff.bluecard;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CarRCActivity extends AppCompatActivity implements BLEControllerListener {
    private TextView statusLog;

    private StringBuilder sb = new StringBuilder();

    private BLEController bleController;

    private boolean isLightOn = false;
    private boolean isMovingAway = false;

    private CarController carController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_r_c);
        this.bleController = BLEController.getInstance();
        this.carController = new CarController((this.bleController));
        CarSteeringView csv = findViewById(R.id.carSteering);
        csv.setCarController(this.carController);
        initUI();
    }

    private void initUI() {
        statusLog = findViewById(R.id.statusLog);
        statusLog.setMovementMethod(new ScrollingMovementMethod());

        findViewById(R.id.ledButton).setOnClickListener(createLEDSwitchOnClickListener());
        findViewById(R.id.disconnectButton).setOnClickListener(createDisconnectOnClickListener());
    }

    private View.OnClickListener createLEDSwitchOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLightOn = !isLightOn;
                carController.switchLED(isLightOn);
                log("Turn light " + (isLightOn?"on":"off"));
            }
        };
    }

    private View.OnClickListener createDisconnectOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleController.disconnect();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.bleController.addBLEControllerListener(this);
        this.isMovingAway = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.isMovingAway = true;
        this.bleController.disconnect();
        this.bleController.removeBLEControllerListener(this);
    }

    private void log(String text) {
        sb.append('\n');
        sb.append(text);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusLog.setText(sb.toString());
            }
        });
    }

    @Override
    public void BLEControllerConnected() { }

    @Override
    public void BLEControllerDisconnected() {
        if(!this.isMovingAway) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CarRCActivity.this, R.string.connection_lost, Toast.LENGTH_SHORT).show();
                }
            });
            Intent intent = new Intent(CarRCActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void BLEDeviceFound(String name, String address) { }
}
