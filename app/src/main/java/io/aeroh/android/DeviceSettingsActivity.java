package io.aeroh.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.aeroh.android.models.Device;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceSettingsActivity extends AppCompatActivity {
    Device device = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);
        Context context = this;

        device = (Device) getIntent().getExtras().get("device");
        TextView device_name = (TextView) findViewById(R.id.device_name);
        Log.i("DeviceSettingsActivity", String.format("Creating settings activity for device: %s", device.thing_name));
        device_name.setText(device.name + " Settings");

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
}
