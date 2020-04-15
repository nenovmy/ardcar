package com.nenoff.ardbttest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements BLEControllerListener {
    private Button connectDeviceButton;
    private TextView response;

    private String deviceAddress;

    private StringBuilder sb = new StringBuilder();

    private BLEController bleController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        response = (TextView) findViewById(R.id.statusLog);
        response.setMovementMethod(new ScrollingMovementMethod());

        connectDeviceButton = (Button) findViewById(R.id.connectDevice);
        connectDeviceButton.setEnabled(false);
        connectDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDeviceButton.setEnabled(false);
                log("Connecting...");
                bleController.connectToDevice(deviceAddress);
            }
        });

        checkPermissions();

        // Check if BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            log("\"Access Fine Location\" permission not granted!");
            log("Whitout this permission Blutooth devices cannot be searched!");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    42);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 42) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                log("Permission \"Access Fine Location\" granted.");
                log("Now searching for BlueArdCar...");
                this.bleController.init();
            } else {
                log("You declined the \"Access Fine Location\" permission!");
                log("Without this permission no bluetooth devices will be found!");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.deviceAddress = null;
        this.bleController = BLEController.getInstance(this);
        this.bleController.addBLEControllerListener(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            log("[BLE]\tSearching for BlueArdCar...");
            this.bleController.init();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.bleController.removeBLEControllerListener(this);
    }

    private void log(String text) {
        sb.append('\n');
        sb.append(text);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                response.setText(sb.toString());
            }
        });
    }

    @Override
    public void BLEControllerConnected() {
        log("[BLE]\tConnected");
        Intent intent = new Intent(this, CarRCActivity.class);
        startActivity(intent);
    }

    @Override
    public void BLEControllerDisconnected() {
        log("[BLE]\tDisconnected");
        this.connectDeviceButton.setEnabled(false);
        this.bleController.init();
    }

    @Override
    public void BLEDeviceFound(String name, String address) {
        log("Device " + name + " found with address " + address);
        this.deviceAddress = address;
        this.connectDeviceButton.setEnabled(true);
    }
}
