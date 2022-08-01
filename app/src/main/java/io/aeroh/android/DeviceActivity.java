package io.aeroh.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Context context = this;

        Device device = (Device) getIntent().getExtras().get("device");
        TextView device_name = (TextView) findViewById(R.id.device_name);
        device_name.setText(device.name);

        createMQTTClient(device);

        Button btnTogglePower = (Button) findViewById(R.id.btnTogglePower);
        btnTogglePower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                String topic = String.format("%s/commands", device.thing_name);
                String [] command = new String[] { "power", "toggle" };
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
        });

        Button btnRemoveDevice = (Button) findViewById(R.id.btnRemoveDevice);
        btnRemoveDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context).
                        setTitle("Confirm Deletion").
                        setMessage("Proceed to delete the device called " + device.name + " with id " + device.mac_addr + " from your account?").
                        setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                new AlertDialog.Builder(context).
                                    setTitle("Confirm Deletion").
                                    setMessage("You will not be able to undo this action. Ok to proceed?").
                                    setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            SharedPreferences shared_preferences = getApplicationContext().getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
                                            String access_token = shared_preferences.getString("API_SERVER_ACCESS_TOKEN", null);
                                            if (access_token != null) {
                                                ApiServer api_server = new ApiServer(access_token);
                                                Call<ResponseBody> call = api_server.devices.delete(device.getId());
                                                call.enqueue(new Callback<ResponseBody>() {
                                                    @Override
                                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                        Log.d("DeviceActivity", "Got response: " + String.valueOf(response.code()));
                                                        setResult(RESULT_OK);
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                    }
                                                });
                                            }
                                        }}).
                                    setNegativeButton(android.R.string.no, null).show();
                            }}).
                        setNegativeButton(android.R.string.no, null).show();
            }
        });
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
