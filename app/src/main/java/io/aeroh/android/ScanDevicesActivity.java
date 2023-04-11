package io.aeroh.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScanDevicesActivity extends AppCompatActivity {
    private static final long TIMEOUT_SCAN = 4000L;

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private List<ScanResult> mBleList;
    private BleAdapter mBleAdapter;

    private Map<String, ScanResult> mDeviceMap;
    private ScanCallback mScanCallback;
    private volatile long mScanStartTime;

    private ExecutorService mThreadPool;
    private Future mUpdateFuture;

    private String mBlufiFilter = "Aeroh";

    private boolean bt_scan_permission_granted = false;
    private boolean bt_connect_permission_granted = false;
    private boolean fine_location_permission_granted = false;

    Button btnScan;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        evaluatePermissions();
        if (!bt_scan_permission_granted || !bt_connect_permission_granted || !fine_location_permission_granted) {
            goToRequestPermission();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_devices);

        mThreadPool = Executors.newSingleThreadExecutor();

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

        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan();
            }
        });
        progressBar = findViewById(R.id.progressBar);

        mRefreshLayout = findViewById(R.id.refresh_layout);
        //mRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mRefreshLayout.setOnRefreshListener(this::scan);

        mRecyclerView = findViewById(R.id.recycler_view);
        mBleList = new LinkedList<>();
        mBleAdapter = new BleAdapter();
        mRecyclerView.setAdapter(mBleAdapter);

        mDeviceMap = new HashMap<>();
        mScanCallback = new ScanCallback();

        scan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
        mThreadPool.shutdownNow();
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

    void goToRequestPermission() {
        Intent intent = new Intent(getApplicationContext(), RequestPermissionActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("MissingPermission")
    private void scan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (!adapter.isEnabled() || scanner == null) {
            Toast.makeText(this, "Bluetooth is disabled!", Toast.LENGTH_SHORT).show();
            mRefreshLayout.setRefreshing(false);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check location enabled
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean locationEnable = locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager);
            if (!locationEnable) {
                Toast.makeText(this, "Location is disabled!", Toast.LENGTH_SHORT).show();
                mRefreshLayout.setRefreshing(false);
                return;
            }
        }

        mDeviceMap.clear();
        mBleList.clear();
        mBleAdapter.notifyDataSetChanged();
        mScanStartTime = SystemClock.elapsedRealtime();

        Log.d("Scan Devices Activity", "Start scan ble");
        scanner.startScan(null, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),
                mScanCallback);
        mUpdateFuture = mThreadPool.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                long scanCost = SystemClock.elapsedRealtime() - mScanStartTime;
                if (scanCost > TIMEOUT_SCAN) {
                    break;
                }

                onIntervalScanUpdate(false);
            }

            BluetoothLeScanner inScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            if (inScanner != null) {
                inScanner.stopScan(mScanCallback);
            }
            onIntervalScanUpdate(true);
            Log.d("ScanDevicesActivity", "Scan ble thread is interrupted");
        });
    }

    @SuppressLint("MissingPermission")
    private void stopScan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner != null) {
            scanner.stopScan(mScanCallback);
        }
        if (mUpdateFuture != null) {
            mUpdateFuture.cancel(true);
        }
        Log.d("ScanDevicesActivity", "Stop scan ble");
    }

    private void onIntervalScanUpdate(boolean over) {
        Log.d("ScanDevicesActivity", "inside onIntervalScanUpdate");
        Log.d("ScanDevicesActivity", "Device count = " + Integer.toString(mDeviceMap.values().size()));
        List<ScanResult> devices = new ArrayList<>(mDeviceMap.values());
        Collections.sort(devices, (dev1, dev2) -> {
            Integer rssi1 = dev1.getRssi();
            Integer rssi2 = dev2.getRssi();
            return rssi2.compareTo(rssi1);
        });
        runOnUiThread(() -> {
            mBleList.clear();
            mBleList.addAll(devices);
            mBleAdapter.notifyDataSetChanged();

            if (over) {
                mRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void gotoDevice(BluetoothDevice device) {
        Log.d("ScanDevicesActivity", "gotoDevice");

        Intent intent = new Intent(getApplicationContext(), ScannedDeviceActivity.class);
        intent.putExtra("bluetooth_device", device);
        startActivity(intent);

        finish();
    }

    private class ScanCallback extends android.bluetooth.le.ScanCallback {

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                onLeScan(result);
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            onLeScan(result);
        }

        private void onLeScan(ScanResult scanResult) {
            String name = scanResult.getDevice().getName();
            if (!TextUtils.isEmpty(mBlufiFilter)) {
                if (name == null || !name.startsWith(mBlufiFilter)) {
                    return;
                }
            }

            mDeviceMap.put(scanResult.getDevice().getAddress(), scanResult);
        }
    }

    private class BleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ScanResult scanResult;
        TextView device_name;
        TextView device_info;

        BleHolder(View itemView) {
            super(itemView);

            device_name = itemView.findViewById(R.id.device_name);
            device_info = itemView.findViewById(R.id.device_info);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            stopScan();
            gotoDevice(scanResult.getDevice());
        }
    }

    private class BleAdapter extends RecyclerView.Adapter<BleHolder> {

        @NonNull
        @Override
        public BleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.scanned_devices_list_item, parent, false);
            return new BleHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BleHolder holder, int position) {
            ScanResult scanResult = mBleList.get(position);
            holder.scanResult = scanResult;

            BluetoothDevice device = scanResult.getDevice();
            @SuppressLint("MissingPermission") String name = device.getName() == null ? "Unknown" : device.getName();
            holder.device_name.setText(name);

            SpannableStringBuilder info = new SpannableStringBuilder();
            info.append("MAC Address: ").append(device.getAddress());
            holder.device_info.setText(info);
        }

        @Override
        public int getItemCount() {
            return mBleList.size();
        }
    }
}
