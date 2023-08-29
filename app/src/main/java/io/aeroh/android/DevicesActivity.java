package io.aeroh.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import io.aeroh.android.models.Device;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DevicesActivity extends AppCompatActivity {
    int Delay = 1000;
    ListView devicesListView;
    DevicesArrayAdapter devicesArrayAdapter;
    ImageView hamburgerIcon;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        updateDevicesList();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(null);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        drawerLayout = findViewById(R.id.DevicesDrawerLayout);
        navigationView = findViewById(R.id.DevicesActivityNavigationView);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        hamburgerIcon = findViewById(R.id.navigationButton);
        actionBarDrawerToggle.syncState();

        hamburgerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Onclick", "Button Clicked");
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.logOutButton) {
                    logout();
                }
                return true;
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
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateDevicesList();
    }

    void updateDevicesList() {
        SharedPreferences shared_preferences = getApplicationContext().getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
        String access_token = shared_preferences.getString("API_SERVER_ACCESS_TOKEN", null);
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

    void openLoginActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        }, Delay);
    }

    void logout() {
        SharedPreferences userAccessPreferences = getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = userAccessPreferences.edit();
        editor.clear().apply();
        openLoginActivity();
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
