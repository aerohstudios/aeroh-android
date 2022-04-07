package io.aeroh.android;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.aeroh.android.models.Device;

public class ScannedDeviceActivity extends AppCompatActivity {

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_device);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("RequestPermissionActivity", "Back button clicked");
                Intent intent = new Intent(getApplicationContext(), ScanDevicesActivity.class);
                startActivity(intent);
                finish();
            }
        });

        BluetoothDevice device = (BluetoothDevice) getIntent().getExtras().get("bluetooth_device");
        TextView device_name = (TextView) findViewById(R.id.textTitle);
        device_name.setText(device.getName());

        TextView mac_address = (TextView) findViewById(R.id.textMacAddress);
        mac_address.setText("MAC Address: " + device.getAddress());
    }
}