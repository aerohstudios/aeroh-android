package io.aeroh.android;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.aeroh.android.models.Device;

import io.aeroh.android.utils.MQTTClient;
import io.aeroh.android.utils.MQTTClient.MQTTClientStatus;

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

        mqttClient = new MQTTClient(getApplicationContext(), device.mqtt_uri, device.thing_name);

        Button btnTogglePower = (Button) findViewById(R.id.btnTogglePower);
        btnTogglePower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject requestMessage = new JSONObject();
                try {
                    requestMessage.put("request_id", UUID.randomUUID());
                    requestMessage.put("command", "power");
                    requestMessage.put("action_type", "toggle");
                } catch (JSONException e) {
                    Log.e("DeviceActivity", e.getLocalizedMessage());
                }
                attachBtnMQTTOnClickListener(view, context, device, requestMessage);
            }
        });

        Button btnSpeedChange = (Button) findViewById(R.id.btnSpeedChange);
        btnSpeedChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject requestMessage = new JSONObject();
                try {
                    requestMessage.put("request_id", UUID.randomUUID());
                    requestMessage.put("command", "speed");
                    requestMessage.put("action_type", "change");
                } catch (JSONException e) {
                    Log.e("DeviceActivity", e.getLocalizedMessage());
                }
                attachBtnMQTTOnClickListener(view, context, device, requestMessage);
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

    @Override
    protected void onResume() {
        super.onResume();
        connectToMQTTServer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mqttClient.disconnect();
    }

    void attachBtnMQTTOnClickListener(View view, Context context, Device device, JSONObject request) {
        view.setEnabled(false);

        String topic = String.format("%s/commands", device.thing_name);

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

        if (mqttClient.mqttClientStatus == MQTTClientStatus.Connected) {
            mqttClient.publish(topic, request.toString(), publishCallback);
        } else if (mqttClient.mqttClientStatus == MQTTClientStatus.Connecting) {
            view.setEnabled(true);
            Toast.makeText(context, "Please wait till we connect to the MQTT Server!", Toast.LENGTH_SHORT).show();
        } else if (mqttClient.mqttClientStatus == MQTTClientStatus.ConnectionFailed) {
            view.setEnabled(true);
            Toast.makeText(context, "Failed to connect to MQTT Server!", Toast.LENGTH_SHORT).show();
        }
    }

    void connectToMQTTServer() {
        mqttClient.connect(new MQTTClient.Callback() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                subscribeToMQTTServer();
                Toast.makeText(getApplicationContext(), "Connected to the MQTT Server!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Toast.makeText(getApplicationContext(), "Failed to connect to MQTT Server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void subscribeToMQTTServer() {
        String topic = String.format("%s/responses", device.thing_name);;
        mqttClient.subscribe(topic, null, null);
    }
}
