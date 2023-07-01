package io.aeroh.android;

import io.aeroh.android.api.meta.Callback;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Animation for the Splash Screen Logo.
        ImageView splash_logo = findViewById(R.id.splash_logo);
        Animation fade_in_and_grow = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_and_grow);
        splash_logo.startAnimation(fade_in_and_grow);

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.d("MainActivity", "Got code!");
            Uri uri = intent.getData();
            String code = uri.getQueryParameter("code");
            getAccessToken(code);
        } else {
            Log.d("MainActivity", "Verify Access Token!");
            verifyAccessToken();
        }
    }

    void openLoginActivity() {
        Log.d("MainActivity", "openLoginActivity");
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    void openDevicesActivity() {
        Log.d("MainActivity", "openDevicesActivity");
        Intent intent = new Intent(getApplicationContext(), DevicesActivity.class);
        startActivity(intent);
        finish();
    }

    void verifyAccessToken() {
        SharedPreferences shared_preferences = getApplicationContext().getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
        String access_token = shared_preferences.getString("API_SERVER_ACCESS_TOKEN", null);
        if (access_token != null) {
            Log.d("MainActivity", "access_token is present");
            ApiServer api_server = new ApiServer(access_token);
            api_server.isAuthenticated(new Callback() {
                @Override
                public void onSuccess() {
                    Log.d("MainActivity", "access_token is valid");
                    openDevicesActivity();
                }

                @Override
                public void onFailure(failureType type, String message) {
                    if (type == failureType.INVALID_TOKEN) {
                        shared_preferences.edit().remove("API_SERVER_ACCESS_TOKEN").apply();
                        openLoginActivity();
                    } else {
                        Log.d("MainActivity", "request failed!");
                        showErrorToast(message);
                    }
                }
            });
        } else {
            Log.d("MainActivity", "access_token is null");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    openLoginActivity();
                }
            }, 2500);
        }
    }

    void getAccessToken(String code) {
        Log.d("MainActivity", "getAccessToken");
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme(BuildConfig.API_SERVER_SCHEME)
                .encodedAuthority(BuildConfig.API_SERVER_HOST)
                .appendPath("oauth")
                .appendPath("token")
                .build();

        Map<String, String> params = new HashMap<>();
        params.put("client_id", BuildConfig.API_SERVER_CLIENT_ID);
        params.put("client_secret", BuildConfig.API_SERVER_CLIENT_SECRET);
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("redirect_uri", BuildConfig.API_SERVER_REDIRECT_URI);

        RequestQueue queue = Volley.newRequestQueue(this);
        Context context = getApplicationContext();
        Log.d("MainActivity", "make request to the sever");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri.toString(), new JSONObject(params), (JSONObject response) -> {
            Log.d("MainActivity", "got response from the sever");
            try {
                String access_token = response.getString("access_token");
                SharedPreferences shared_preferences = context.getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
                shared_preferences.edit().putString("API_SERVER_ACCESS_TOKEN", access_token).apply();
                verifyAccessToken();
            } catch (Exception e) {
                String message = e.getMessage();
                Log.d("MainActivity", "exception occurred!");
                showErrorToast(message);
            }
        }, (VolleyError error) -> {
            Log.d("MainActivity", "request failed!");
            String message = error.getMessage();
            showErrorToast(message);
        });

        queue.add(jsonObjectRequest);
    }

    void showErrorToast(String message) {
        Log.d("MainActivity", message);
        Toast errorToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        errorToast.show();
    }
}