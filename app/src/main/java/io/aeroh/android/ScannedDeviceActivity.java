package io.aeroh.android;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import blufi.espressif.BlufiCallback;
import blufi.espressif.BlufiClient;
import blufi.espressif.params.BlufiConfigureParams;
import blufi.espressif.params.BlufiParameter;
import blufi.espressif.response.BlufiScanResult;
import blufi.espressif.response.BlufiStatusResponse;
import blufi.espressif.response.BlufiVersionResponse;

public class ScannedDeviceActivity extends AppCompatActivity {
    private BlufiClient mBlufiClient = null;
    private BluetoothDevice mDevice = null;

    private TextView provision_status;

    public static final int REQUEST_WIFI_CREDENTIALS = 0x01;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_device);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ScannedDeviceActivity", "Back button clicked");
                Intent intent = new Intent(getApplicationContext(), ScanDevicesActivity.class);
                startActivity(intent);
                finish();
            }
        });

        BluetoothDevice mDevice = (BluetoothDevice) getIntent().getExtras().get("bluetooth_device");
        TextView device_name = (TextView) findViewById(R.id.textTitle);
        device_name.setText(mDevice.getName());

        TextView mac_address = (TextView) findViewById(R.id.textMacAddress);
        mac_address.setText("MAC Address: " + mDevice.getAddress());

        Button btnProvision = (Button) findViewById(R.id.btnProvision);
        btnProvision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ScannedDeviceActivity", "Provision button clicked!");
                btnProvision.setEnabled(false);

                if (mBlufiClient != null) {
                    mBlufiClient.close();
                    mBlufiClient = null;
                }

                mBlufiClient = new BlufiClient(getApplicationContext(), mDevice);
                mBlufiClient.setGattCallback(new GattCallback());
                mBlufiClient.setBlufiCallback(new BlufiCallbackMain());
                Log.d("ScannedDeviceActivity", "BT Connect!");
                mBlufiClient.connect();
            }
        });

        provision_status = (TextView) findViewById(R.id.textProvisionStatus);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBlufiClient != null) {
            mBlufiClient.close();
            mBlufiClient = null;
        }
    }

    private void onGattServiceCharacteristicDiscovered() {
        Log.d("ScannedDeviceActivity", "BT Negotiate Security");
        mBlufiClient.negotiateSecurity();
    }

    private void onSecurityNegotiationComplete() {
        if (hasCredentialsInSharedPreferences()) {
            setCredentialsFromSharedPreferences();
        } else {
            Intent intent = new Intent(getApplicationContext(), WifiCredentialsActivity.class);
            startActivityForResult(intent, REQUEST_WIFI_CREDENTIALS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("ScannedDeviceActivity", "onActivityResult");
        if (requestCode == REQUEST_WIFI_CREDENTIALS) {
            if (resultCode == RESULT_OK) {
                Log.d("ScannedDeviceActivity", "Got credentials!");

                setCredentialsFromSharedPreferences();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    private boolean hasCredentialsInSharedPreferences() {
        SharedPreferences shared_preferences = getApplicationContext().getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
        String ssid = shared_preferences.getString("WIFI_SSID", null);
        String password = shared_preferences.getString("WIFI_PASSWORD", null);

        return (ssid != null && password != null);
    }

    private void setCredentialsFromSharedPreferences() {
        Log.d("ScannedDeviceActivity", "setCredentialsFromSharedPreferences");
        SharedPreferences shared_preferences = getApplicationContext().getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
        String ssid = shared_preferences.getString("WIFI_SSID", null);
        String password = shared_preferences.getString("WIFI_PASSWORD", null);

        BlufiConfigureParams params = new BlufiConfigureParams();
        params.setOpMode(BlufiParameter.OP_MODE_STA);
        params.setStaPassword(password);
        params.setStaSSIDBytes(ssid.getBytes());
        Log.d("ScannedDeviceActivity", "Configure Wifi Credentials");
        mBlufiClient.configure(params);
    }

    private void onWifiConnectionSuccess() {
        Log.d("ScannedDeviceActivity", "Send Aeroh Cloud Access Token");

    }

    private void updateMessage(String message, boolean isNotificaiton) {
        runOnUiThread(() -> {
            String currentText = (String) provision_status.getText();
            provision_status.setText(currentText + "\n" + message);
        });
    }

    private class GattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String devAddr = gatt.getDevice().getAddress();
            Log.d("ScannedDeviceActivity", String.format(Locale.ENGLISH, "onConnectionStateChange addr=%s, status=%d, newState=%d",
                    devAddr, status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        //onGattConnected();
                        updateMessage(String.format("Connected %s", devAddr), false);
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        gatt.close();
                        //onGattDisconnected();
                        updateMessage(String.format("Disconnected %s", devAddr), false);
                        break;
                }
            } else {
                gatt.close();
                //onGattDisconnected();
                updateMessage(String.format(Locale.ENGLISH, "Disconnect %s, status=%d", devAddr, status),
                        false);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.d("ScannedDeviceActivity", String.format(Locale.ENGLISH, "onMtuChanged status=%d, mtu=%d", status, mtu));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                updateMessage(String.format(Locale.ENGLISH, "Set mtu complete, mtu=%d ", mtu), false);
            } else {
                mBlufiClient.setPostPackageLengthLimit(20);
                updateMessage(String.format(Locale.ENGLISH, "Set mtu failed, mtu=%d, status=%d", mtu, status), false);
            }

            onGattServiceCharacteristicDiscovered();
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d("ScannedDeviceActivity", String.format(Locale.ENGLISH, "onServicesDiscovered status=%d", status));
            if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
                updateMessage(String.format(Locale.ENGLISH, "Discover services error status %d", status), false);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d("ScannedDeviceActivity", String.format(Locale.ENGLISH, "onDescriptorWrite status=%d", status));
            if (descriptor.getUuid().equals(BlufiParameter.UUID_NOTIFICATION_DESCRIPTOR) &&
                    descriptor.getCharacteristic().getUuid().equals(BlufiParameter.UUID_NOTIFICATION_CHARACTERISTIC)) {
                String msg = String.format(Locale.ENGLISH, "Set notification enable %s", (status == BluetoothGatt.GATT_SUCCESS ? "complete" : "failed"));
                updateMessage(msg, false);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
                updateMessage(String.format(Locale.ENGLISH, "WriteChar error status %d", status), false);
            }
        }
    }

    private class BlufiCallbackMain extends BlufiCallback {
        @SuppressLint("MissingPermission")
        @Override
        public void onGattPrepared(BlufiClient client, BluetoothGatt gatt, BluetoothGattService service,
                                   BluetoothGattCharacteristic writeChar, BluetoothGattCharacteristic notifyChar) {
            if (service == null) {
                Log.w("ScannedDeviceActivity", "Discover service failed");
                gatt.disconnect();
                updateMessage("Discover service failed", false);
                return;
            }
            if (writeChar == null) {
                Log.w("ScannedDeviceActivity", "Get write characteristic failed");
                gatt.disconnect();
                updateMessage("Get write characteristic failed", false);
                return;
            }
            if (notifyChar == null) {
                Log.w("ScannedDeviceActivity", "Get notification characteristic failed");
                gatt.disconnect();
                updateMessage("Get notification characteristic failed", false);
                return;
            }

            updateMessage("Discover service and characteristics success", false);

            int mtu = 512;
            Log.d("ScannedDeviceActivity", "Request MTU " + mtu);
            boolean requestMtu = gatt.requestMtu(mtu);
            if (!requestMtu) {
                Log.w("ScannedDeviceActivity", "Request mtu failed");
                updateMessage(String.format(Locale.ENGLISH, "Request mtu %d failed", mtu), false);
                onGattServiceCharacteristicDiscovered();
            }
        }

        @Override
        public void onNegotiateSecurityResult(BlufiClient client, int status) {
            if (status == STATUS_SUCCESS) {
                updateMessage("Negotiate security complete", false);
                onSecurityNegotiationComplete();
            } else {
                updateMessage("Negotiate security failed， code=" + status, false);
            }

            //mBlufiSecurityBtn.setEnabled(mConnected);
        }

        @Override
        public void onPostConfigureParams(BlufiClient client, int status) {
            if (status == STATUS_SUCCESS) {
                updateMessage("Post configure params complete", false);
            } else {
                updateMessage("Post configure params failed, code=" + status, false);
            }

            //mBlufiConfigureBtn.setEnabled(mConnected);
        }

        @Override
        public void onDeviceStatusResponse(BlufiClient client, int status, BlufiStatusResponse response) {
            if (status == STATUS_SUCCESS) {
                updateMessage(String.format("Receive device status response:\n%s", response.generateValidInfo()),
                        true);
                onWifiConnectionSuccess();
            } else {
                updateMessage("Device status response error, code=" + status, false);
            }

            //mBlufiDeviceStatusBtn.setEnabled(mConnected);
        }

        @Override
        public void onDeviceScanResult(BlufiClient client, int status, List<BlufiScanResult> results) {
            if (status == STATUS_SUCCESS) {
                StringBuilder msg = new StringBuilder();
                msg.append("Receive device scan result:\n");
                for (BlufiScanResult scanResult : results) {
                    msg.append(scanResult.toString()).append("\n");
                }
                updateMessage(msg.toString(), true);
            } else {
                updateMessage("Device scan result error, code=" + status, false);
            }

            //mBlufiDeviceScanBtn.setEnabled(mConnected);
        }

        @Override
        public void onDeviceVersionResponse(BlufiClient client, int status, BlufiVersionResponse response) {
            if (status == STATUS_SUCCESS) {
                updateMessage(String.format("Receive device version: %s", response.getVersionString()),
                        true);
            } else {
                updateMessage("Device version error, code=" + status, false);
            }

            //mBlufiVersionBtn.setEnabled(mConnected);
        }

        @Override
        public void onPostCustomDataResult(BlufiClient client, int status, byte[] data) {
            String dataStr = new String(data);
            String format = "Post data %s %s";
            if (status == STATUS_SUCCESS) {
                updateMessage(String.format(format, dataStr, "complete"), false);
            } else {
                updateMessage(String.format(format, dataStr, "failed"), false);
            }
        }

        @Override
        public void onReceiveCustomData(BlufiClient client, int status, byte[] data) {
            if (status == STATUS_SUCCESS) {
                String customStr = new String(data);
                updateMessage(String.format("Receive custom data:\n%s", customStr), true);
            } else {
                updateMessage("Receive custom data error, code=" + status, false);
            }
        }

        @Override
        public void onError(BlufiClient client, int errCode) {
            updateMessage(String.format(Locale.ENGLISH, "Receive error code %d", errCode), false);
        }
    }
}