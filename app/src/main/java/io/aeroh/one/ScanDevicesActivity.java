package io.aeroh.one;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ScanDevicesActivity extends AppCompatActivity {
    BluetoothAdapter mBtAdapter;
    BtBroadcastReceiver mBtReceiver;

    ScannedDevicesArrayAdapter mDevicesAdapter;

    Button btnScan;
    ProgressBar progressBar;

    static final int accessCoarseLocationRequestCode = 1;
    static final int enableBluetoothRequestCode = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_devices);

        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initDiscovery();
            }
        });
        progressBar = findViewById(R.id.progressBar);

        ListView devicesListView = findViewById(R.id.scannedDevices);
        mDevicesAdapter = new ScannedDevicesArrayAdapter(getApplicationContext());
        devicesListView.setAdapter(mDevicesAdapter);

        initDiscovery();
    }

    private void initDiscovery() {
        boolean canStartDiscovery = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, accessCoarseLocationRequestCode);
                canStartDiscovery = false;
            }
        }

        if (canStartDiscovery) {
            startBtDiscovery();
        }
    }

    private void startBtDiscovery() {
        // example code for bluetooth scanning
        // https://programmer.group/5e841c1909e93.html
        // bt official permission guide
        // https://developer.android.com/about/versions/12/features/bluetooth-permissions

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Log.e("Bluetooth", "Device doesn't support it");
        }

        if (!mBtAdapter.isEnabled()) {
            Log.e("Bluetooth", "Not enabled on the device");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, enableBluetoothRequestCode);
        }

        mBtReceiver = new BtBroadcastReceiver();
        registerReceiver(mBtReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mBtReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(mBtReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        if (mBtAdapter.isDiscovering()) {
            Log.e("Bluetooth", "Discovery Already Started. Stopping.");
            mBtAdapter.cancelDiscovery();
        }

        mBtAdapter.startDiscovery();
    }

    private class BtBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                if (deviceName != null) {
                    Log.e("Found BT Device Name = ", deviceName);
                    Log.e("Found BT Device Addr = ", deviceHardwareAddress);
                    if (mDevicesAdapter.getPosition(device) < 0) {
                        mDevicesAdapter.add(device);
                        mDevicesAdapter.notifyDataSetChanged();
                    }
                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.e("Bluetooth", "Discovery Started");
                btnScan.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e("Bluetooth", "Discovery Finished");
                btnScan.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }
    };

    static class ScannedDevicesArrayAdapter extends ArrayAdapter<BluetoothDevice> {
        public ScannedDevicesArrayAdapter(@NonNull Context context) {
            super(context, 0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.scanned_devices_list_item, parent, false);
            }
            BluetoothDevice device = getItem(position);
            TextView name = convertView.findViewById(R.id.name);
            TextView mac_addr = convertView.findViewById(R.id.mac_addr);
            name.setText(device.getName());
            mac_addr.setText(device.getAddress());
            return convertView;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        unregisterReceiver(mBtReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.e("Inside", "REquest coast permission result");
        switch (requestCode) {
            case accessCoarseLocationRequestCode:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startBtDiscovery();
                }

                break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }
}
