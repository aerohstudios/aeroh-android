package io.aeroh.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class RequestPermissionActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            goToScanDevices();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_permission);


        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("RequestPermissionActivity", "Back button clicked");
                Intent intent = new Intent(getApplicationContext(), DevicesActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("RequestPermissionActivity", "Continue button clicked");
                ActivityCompat.requestPermissions(RequestPermissionActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("RequestPermissionActivity", "onRequestPermissionResult callback");
        int size = permissions.length;
        for (int i = 0; i < size; ++i) {
            String permission = permissions[i];
            int grant = grantResults[i];

            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grant == PackageManager.PERMISSION_GRANTED) {
                    Log.d("RequestPermissionActivity", "Permission Granted!");
                    goToScanDevices();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void goToScanDevices() {
        Intent intent = new Intent(getApplicationContext(), ScanDevicesActivity.class);
        startActivity(intent);
        finish();
    }
}