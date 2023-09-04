package io.aeroh.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;


import io.aeroh.android.models.Device;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DevicesActivity extends AppCompatActivity {
    ListView devicesListView;
    DevicesArrayAdapter devicesArrayAdapter;
    DrawerLayout hamburgerDrawer;
    ImageView hamburgerToggle;
    Toolbar activityToolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        updateDevicesList();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        hamburgerDrawer = findViewById(R.id.devicesActivityDrawer);
        activityToolbar = findViewById(R.id.devicesActivityToolbar);
        setSupportActionBar(activityToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, hamburgerDrawer, R.string.drawer_open_string, R.string.drawer_close_string);

        hamburgerToggle = findViewById(R.id.hamburgerIcon);
        hamburgerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hamburgerDrawer.openDrawer(GravityCompat.END);
            }
        });

        devicesListView = findViewById(R.id.devices_list_view);
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d("DevicesActivity", "List item clicked!");
                Device device = devicesArrayAdapter.getItem(position);
                Intent intent = new Intent(getApplicationContext(), DeviceActivity.class);
                intent.putExtra("device", device);
                startActivity(intent);
            }
        });

        Button btnAddDevice = findViewById(R.id.btnAddDevice);
        btnAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DevicesActivity", "Add Device button clicked!");
                Intent intent = new Intent(getApplicationContext(), RequestPermissionActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDevicesList();
    }

    void updateDevicesList() {
        SharedPreferences shared_preferences = getApplicationContext().getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
        String access_token = shared_preferences.getString("access_token", null);
        if (access_token != null) {
            ApiServer api_server = new ApiServer(access_token);
            Call<List<Device>> call = api_server.devices.list();
            call.enqueue(new Callback<List<Device>>() {
                @Override
                public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                    int statusCode = response.code();
                    if (statusCode == 200) {
                        List<Device> devices = response.body();
                        populateDevicesList(devices);
                    } else if (statusCode == 401) {
                        // TODO: Redirect to Login
                    }
                }

                @Override
                public void onFailure(Call<List<Device>> call, Throwable t) {
                    // TODO: Show Server Error
                }
            });
        }
    }

    void populateDevicesList(List<Device> devices) {
        devicesArrayAdapter = new DevicesArrayAdapter(getApplicationContext(), devices);
        devicesListView.setAdapter(devicesArrayAdapter);
    }

    static class DevicesArrayAdapter extends ArrayAdapter<Device> {

        public DevicesArrayAdapter(@NonNull Context context, @NonNull List<Device> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.devices_list_item, parent, false);
            }
            Device device = getItem(position);
            TextView name = convertView.findViewById(R.id.name);
            TextView mac_addr = convertView.findViewById(R.id.mac_addr);
            name.setText(device.name);
            mac_addr.setText(device.mac_addr);
            return convertView;
        }
    }
}
