package io.aeroh.android;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONArray;

import java.util.Arrays;
import java.util.HashMap;

import io.aeroh.android.models.Device;

import io.aeroh.android.utils.MQTTClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceActivity extends AppCompatActivity {
    MQTTClient mqttClient = null;
    Device device = null;

    @Override
    protected void onStop() {
        Log.i("Device Activity", "onStop called");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("Device Activity", "onDestroy called");
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Context context = this;

        device = (Device) getIntent().getExtras().get("device");
        TextView device_name = (TextView) findViewById(R.id.device_name);
        Log.i("DeviceActivity", String.format("Creating activity for device: %s", device.thing_name));
        device_name.setText(device.name);

        createMQTTClient(device);

        Button btnTogglePower = (Button) findViewById(R.id.btnTogglePower);
        btnTogglePower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String [] command = new String[] { "power", "toggle" };
                attachBtnMQTTOnClickListener(view, context, device, command);
            }
        });

        Button btnSpeedChange = (Button) findViewById(R.id.btnSpeedChange);
        btnSpeedChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String [] command = new String[] { "speed", "change" };
                attachBtnMQTTOnClickListener(view, context, device, command);
            }
        });


        ActivityResultLauncher<Intent> settingsActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == DeviceSettingsActivity.RESULT_RENAMED) {
                            Log.d("DeviceAcivity", "Renamed");
                            Intent data = result.getData();
                            device_name.setText(data.getExtras().getString("device_name"));
                        } else if (result.getResultCode() == DeviceSettingsActivity.RESULT_DELETED) {
                            Log.d("DeviceAcivity", "Deleted! Destroying Activity!");
                            finish();
                        }
                    }
                });

        Button btnDeviceSettings = (Button) findViewById(R.id.btnDeviceSettings);
        btnDeviceSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DeviceActivity", "Show DeviceSettingsActivity!");
                Intent intent = new Intent(getApplicationContext(), DeviceSettingsActivity.class);
                intent.putExtra("device", device);
                settingsActivityResultLauncher.launch(intent);
            }
        });
    }

    void attachBtnMQTTOnClickListener(View view, Context context, Device device, String[] command) {
        view.setEnabled(false);

        String topic = String.format("%s/commands", device.thing_name);
        JSONArray jsonCommand = new JSONArray(Arrays.asList(command));
        String message = jsonCommand.toString();

        MQTTClient.Callback publishCallback = new MQTTClient.Callback() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                view.setEnabled(true);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                view.setEnabled(true);
                Toast.makeText(context, "Failed to publish message on MQTT Server!", Toast.LENGTH_SHORT).show();
            }
        };

        if (mqttClient.isConnected()) {
            mqttClient.publish(topic, message, publishCallback);
        } else {
            mqttClient.connect(new MQTTClient.Callback() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttClient.publish(topic, message, publishCallback);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    view.setEnabled(true);
                    Toast.makeText(context, "Failed to connect to MQTT Server!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void createMQTTClient(Device device) {
        Uri mqttUri = Uri.parse(device.mqtt_uri);
        String host = mqttUri.getHost();
        String scheme = "wss";
        int port = 443;
        String mqttUriStr = String.format("%s://%s:%d", scheme, host, port);

        HashMap<String, String> headers = new HashMap<String, String>();

        SharedPreferences shared_preferences = getApplicationContext().getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
        String access_token = shared_preferences.getString("API_SERVER_ACCESS_TOKEN", null);
        if (access_token != null) {
            headers.put("X-Aeroh-Oauth2-Access-Token", access_token);
        }

        String[] aerohDevices = new String[] { device.thing_name };
        headers.put("X-Aeroh-Devices", TextUtils.join(",", aerohDevices));

        mqttClient = new MQTTClient(getApplicationContext(), mqttUriStr, headers);
    }
}
