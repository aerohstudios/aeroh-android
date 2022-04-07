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
    private static final int REQUEST_ALL_PERMISSIONS = 0x01;

    private boolean bt_scan_permission_granted = false;
    private boolean bt_connect_permission_granted = false;
    private boolean fine_location_permission_granted = false;

    private boolean show_manual_setup_instructions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        evaluatePermissions();
        goToNextActivityIfPossible();

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
                if (!goToNextActivityIfPossible()) {
                    ActivityCompat.requestPermissions(
                            RequestPermissionActivity.this,
                            new String[]{
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            REQUEST_ALL_PERMISSIONS
                    );
                }
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

            if (permission.equals(Manifest.permission.BLUETOOTH_SCAN)) {
                if (grant == PackageManager.PERMISSION_GRANTED) {
                    Log.d("RequestPermissionActivity", "Scan Permission Granted!");
                    bt_scan_permission_granted = true;
                } else if (grant == PackageManager.PERMISSION_DENIED) {
                    Log.d("RequestPermissionActivity", "BT Scan Permission Denied!");
                    show_manual_setup_instructions = true;
                }
            }

            if (permission.equals(Manifest.permission.BLUETOOTH_CONNECT)) {
                if (grant == PackageManager.PERMISSION_GRANTED) {
                    Log.d("RequestPermissionActivity", "BT Connect Permission Granted!");
                    bt_connect_permission_granted = true;
                } else if (grant == PackageManager.PERMISSION_DENIED) {
                    Log.d("RequestPermissionActivity", "BT Connect Permission Denied!");
                    show_manual_setup_instructions = true;
                }
            }

            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grant == PackageManager.PERMISSION_GRANTED) {
                    Log.d("RequestPermissionActivity", "Fine Location Permission Granted!");
                    fine_location_permission_granted = true;
                } else if (grant == PackageManager.PERMISSION_DENIED) {
                    Log.d("RequestPermissionActivity", "Fine Location Permission Denied!");
                    show_manual_setup_instructions = true;
                }
            }
        }

        goToNextActivityIfPossible();

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void evaluatePermissions() {
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(),
                Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED) {
            bt_scan_permission_granted = true;
        }

        if (ContextCompat.checkSelfPermission(
                getApplicationContext(),
                Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED) {
            bt_connect_permission_granted = true;
        }

        if (ContextCompat.checkSelfPermission(
                getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {
            fine_location_permission_granted = true;
        }
    }

    boolean goToNextActivityIfPossible() {
        if (bt_scan_permission_granted && bt_connect_permission_granted && fine_location_permission_granted) {
            goToScanDevices();
            return true;
        } else if (show_manual_setup_instructions) {
            goToSetManualPermission();
            return true;
        } else {
            return false;
        }
    }

    void goToScanDevices() {
        Intent intent = new Intent(getApplicationContext(), ScanDevicesActivity.class);
        startActivity(intent);
        finish();
    }

    void goToSetManualPermission() {
        Intent intent = new Intent(getApplicationContext(), SetManualPermissionActivity.class);
        startActivity(intent);
        finish();
    }
}