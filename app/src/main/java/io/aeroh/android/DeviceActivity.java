package io.aeroh.android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import io.aeroh.android.models.Device;

public class DeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        Device device = (Device) getIntent().getExtras().get("device");
        TextView device_name = (TextView) findViewById(R.id.device_name);
        device_name.setText(device.name);
    }
}