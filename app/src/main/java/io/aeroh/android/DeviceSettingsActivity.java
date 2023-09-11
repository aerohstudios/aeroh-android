package io.aeroh.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import io.aeroh.android.models.Device;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceSettingsActivity extends AppCompatActivity {
    Device device = null;
    public static final int RESULT_DELETED = 1;
    public static final int RESULT_RENAMED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);
        Context context = this;

        device = (Device) getIntent().getExtras().get("device");
        Log.i("DeviceSettingsActivity", String.format("Creating settings activity for device: %s", device.thing_name));

        Button backbtn = findViewById(R.id.btnBack);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), DeviceActivity.class));
                finish();
            }
        });

        TextView btnRenameDevice = (TextView) findViewById(R.id.btnRenameDevice);
        btnRenameDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("What do you want to call this device?");

                // Set up the input
                final EditText input = new EditText(context);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(device.name);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        device.name = input.getText().toString();
                        Log.d("DeviceSettingsActivity", "Create Create Device Post Request");

                        SharedPreferences shared_preferences = getApplicationContext().getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
                        String access_token = shared_preferences.getString("access_token", null);
                        ApiServer api_server = new ApiServer(access_token);

                        Device putDevice = new Device();
                        putDevice.setId(device.getId());
                        putDevice.name = device.name;

                        Call<Device> call = api_server.devices.put(device.getId(), putDevice);
                        Log.d("DeviceSettingsActivity", "Enqueue Create Device Post Request");
                        call.enqueue(new Callback<Device>() {
                            @Override
                            public void onResponse(Call<Device> call, Response<Device> response) {
                                int statusCode = response.code();
                                if (statusCode == 200) {
                                    Log.d("DeviceSettingsActivity", String.format("Post Response: 200"));
                                    Intent data = new Intent();
                                    data.putExtra("device_name", device.name);
                                    setResult(RESULT_RENAMED, data);
                                } else if (statusCode == 401) {
                                    // TODO: Redirect to Login
                                    Log.d("DeviceSettingsActivity", String.format("Update Response Failure: 401"));
                                } else {
                                    Log.d("DeviceSettingsActivity", String.format("Update Response Failure: %d", statusCode));
                                }
                            }

                            @Override
                            public void onFailure(Call<Device> call, Throwable t) {
                                // TODO: Show Server Error
                                Log.d("DeviceSettingsActivity", String.format("Update Response Failure: 5XX"));
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        TextView btnUpdateFirmware = (TextView) findViewById(R.id.btnUpdateFirmware);
        btnUpdateFirmware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateFirmwareIntent = new Intent(getApplicationContext(), FirmwareUpdateActivity.class);
                updateFirmwareIntent.putExtra("device", device);
                startActivity(updateFirmwareIntent);
            }
        });

        TextView btnRemoveDevice = (TextView) findViewById(R.id.btnRemoveDevice);
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
                                                String access_token = shared_preferences.getString("access_token", null);
                                                if (access_token != null) {
                                                    ApiServer api_server = new ApiServer(access_token);
                                                    Call<ResponseBody> call = api_server.devices.delete(device.getId());
                                                    call.enqueue(new Callback<ResponseBody>() {
                                                        @Override
                                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                            Log.d("DeviceActivity", "Got response: " + String.valueOf(response.code()));
                                                            setResult(RESULT_DELETED);
                                                            finish();
                                                        }

                                                        @Override
                                                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                        }
                                                    });
                                                }
                                            }
                                        }).
                                        setNegativeButton(android.R.string.no, null).show();
                            }
                        }).
                        setNegativeButton(android.R.string.no, null).show();
            }
        });
    }
}
