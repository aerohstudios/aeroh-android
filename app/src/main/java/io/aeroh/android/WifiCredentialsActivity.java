package io.aeroh.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WifiCredentialsActivity extends AppCompatActivity {
    private WifiManager wifiManager;
    private static final int REQUEST_WIFI_PERMISSIONS = 0x01;
    private Context context;
    AutoCompleteTextView textSsid;
    TextView textMessage;
    LinearLayout ssidLayout, passwordLayout, buttonLayout;
    Button btnSave;
    EditText textPassword;

    private boolean wifi_access_state_permission_granted = false;
    private boolean wifi_change_state_permission_granted = false;
    private boolean show_manual_setup_instructions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_credentials);
        context = getApplicationContext();
        wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        textMessage = (TextView) findViewById(R.id.textMessage);
        ssidLayout = (LinearLayout) findViewById(R.id.ssidLayout);
        passwordLayout = (LinearLayout) findViewById(R.id.passwordLayout);
        buttonLayout = (LinearLayout) findViewById(R.id.buttonLayout);
        textSsid = (AutoCompleteTextView) findViewById(R.id.wifi_ssid);
        textPassword = (EditText) findViewById(R.id.password);

        textMessage.setText("Looking for 2.4 Ghz Wifi Networks around you...");
        ssidLayout.setVisibility(View.GONE);
        passwordLayout.setVisibility(View.GONE);
        buttonLayout.setVisibility(View.GONE);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ssid = textSsid.getText().toString();
                String password = textPassword.getText().toString();

                SharedPreferences shared_preferences = context.getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
                shared_preferences.edit().
                        putString("WIFI_SSID", ssid).
                        putString("WIFI_PASSWORD", password).apply();

                Intent intent = new Intent();
                intent.putExtra("ssid", ssid);
                intent.putExtra("password", password);

                setResult(RESULT_OK, intent);
                finish();
            }
        });

        permissionCheck();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("WifiCredentialsActivity", "onRequestPermissionResult callback");
        int size = permissions.length;
        for (int i = 0; i < size; ++i) {
            String permission = permissions[i];
            int grant = grantResults[i];

            if (permission.equals(Manifest.permission.ACCESS_WIFI_STATE)) {
                if (grant == PackageManager.PERMISSION_GRANTED) {
                    Log.d("WifiCredentialsActivity", "Wifi Access Permission Granted!");
                    wifi_access_state_permission_granted = true;
                } else if (grant == PackageManager.PERMISSION_DENIED) {
                    Log.d("WifiCredentialsActivity", "Wifi Access Permission Denied!");
                    show_manual_setup_instructions = true;
                }
            }

            if (permission.equals(Manifest.permission.CHANGE_WIFI_STATE)) {
                if (grant == PackageManager.PERMISSION_GRANTED) {
                    Log.d("WifiCredentialsActivity", "Wifi Change Permission Granted!");
                    wifi_change_state_permission_granted = true;
                } else if (grant == PackageManager.PERMISSION_DENIED) {
                    Log.d("WifiCredentialsActivity", "Wifi Change Permission Denied!");
                    show_manual_setup_instructions = true;
                }
            }
        }

        permissionCheck();

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void permissionCheck() {
        if (hasWifiPermissions()) {
            startScan();
        } else if (show_manual_setup_instructions) {
            showManualSetupInstructions();
        } else {
            ActivityCompat.requestPermissions(
                    WifiCredentialsActivity.this,
                    new String[]{
                            Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.ACCESS_WIFI_STATE
                    },
                    REQUEST_WIFI_PERMISSIONS
            );
        }
    }

    void evaluatePermissions() {
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(),
                Manifest.permission.ACCESS_WIFI_STATE
        ) == PackageManager.PERMISSION_GRANTED) {
            wifi_access_state_permission_granted = true;
        }

        if (ContextCompat.checkSelfPermission(
                getApplicationContext(),
                Manifest.permission.CHANGE_WIFI_STATE
        ) == PackageManager.PERMISSION_GRANTED) {
            wifi_change_state_permission_granted = true;
        }
    }

    boolean hasWifiPermissions() {
        evaluatePermissions();
        return (wifi_access_state_permission_granted && wifi_change_state_permission_granted);
    }

    void showManualSetupInstructions() {
        Intent intent = new Intent(getApplicationContext(), SetManualPermissionActivity.class);
        startActivity(intent);
        finish();
    }

    private void startScan() {
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wifi disabled! Please enable wifi and try again!", Toast.LENGTH_SHORT).show();
            return;
        }

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        }
    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        Collections.sort(results, new Comparator<ScanResult>(){
            public int compare(ScanResult sr1, ScanResult sr2){
                return sr2.level - sr1.level;
            }
        });

        List<String> wifiSsidsList = new ArrayList<String>();
        for (ScanResult scanResult: results) {
            if (scanResult.frequency >= 2400 && scanResult.frequency < 2500) {
                String ssid = scanResult.SSID;
                if (ssid != null && ssid.length() > 0) {
                    Log.d("WifiCredentialsActivity", ssid + " dBm: "+ Integer.toString(scanResult.level));
                    wifiSsidsList.add(ssid.toString());
                }
            }
        }

        Log.d("WifiCredentialsActivity", String.format("Found %d networks!", wifiSsidsList.size()));
        String[] wifiSsidsArray = new String[wifiSsidsList.size()];
        wifiSsidsList.toArray(wifiSsidsArray);

        runOnUiThread(() -> {
            Log.d("WifiCredentialsActivity", String.format("First Ssid: %s", wifiSsidsArray[0]));
            textSsid.setText(wifiSsidsArray[0]);
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, wifiSsidsArray);
            textSsid.setAdapter(adapter);
            textMessage.setText(String.format("Found %d networks! Selecting one with the strongest signal! Edit to change! Autocomplete supported!", wifiSsidsList.size()));
            ssidLayout.setVisibility(View.VISIBLE);
            passwordLayout.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.VISIBLE);
        });
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
    }
}