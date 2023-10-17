package io.aeroh.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DeviceInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        LinearLayout btnDeviceNameScreen = (LinearLayout) findViewById(R.id.btnDeviceName);
        btnDeviceNameScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent DeviceRenameIntent = new Intent(getApplicationContext(), EditDeviceNameActivity.class);
                startActivity(DeviceRenameIntent);
            }
        });

        LinearLayout btnRoomNameScreen = (LinearLayout) findViewById(R.id.btnRoomName);
        btnRoomNameScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent RoomRenameIntent = new Intent(getApplicationContext(), DeviceRoomNameActivity.class);
                startActivity(RoomRenameIntent);
            }
        });

        LinearLayout btnDeviceTypeScreen = (LinearLayout) findViewById(R.id.btnDeviceType);
        btnDeviceTypeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ChangeDeviceTypeIntent = new Intent(getApplicationContext(), EditDeviceTypeScreen.class);
                startActivity(ChangeDeviceTypeIntent);
            }
        });
    }

}