package com.awishkara.druid_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.awishkara.druid_android.models.Device;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DevicesActivity extends AppCompatActivity {
    ListView devicesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences shared_preferences = getApplicationContext().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        devicesListView = findViewById(R.id.devices_list_view);
    }

    void populateDevicesList(List<Device> devices) {
        DevicesArrayAdapter devicesArrayAdapter = new DevicesArrayAdapter(getApplicationContext(), devices);
        devicesListView.setAdapter(devicesArrayAdapter);
    }

    class DevicesArrayAdapter extends ArrayAdapter<Device> {

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
