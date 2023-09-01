package io.aeroh.android;

import android.annotation.SuppressLint;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.aeroh.android.api.meta.Callback;
import io.aeroh.android.utils.NetworkStatus;

@SuppressLint("CustomSplashScreen")
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
        if (!NetworkStatus.isInternetConnected(getApplicationContext())) {
            showErrorDialogue("Unable to access Internet!");
        } else {
            Log.d("MainActivity", "Verify Access Token!");
            verifyAccessToken();
        }
    }

    void openDevicesActivity() {
        Log.d("MainActivity", "openDevicesActivity");
        Intent intent = new Intent(getApplicationContext(), DevicesActivity.class);
        startActivity(intent);
        finish();
    }

    void openOnboardActivity() {
        Log.d("SplashActivity", "openOnboardActivity");
        Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    void verifyAccessToken() {
        SharedPreferences userAccessPreference = getApplicationContext().getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
        String access_token = userAccessPreference.getString("access_token", null);
        if (access_token != null) {
            Log.d("MainActivity", "access_token is present");
            ApiServer apiServer = new ApiServer(access_token);
            apiServer.isAuthenticated(new Callback() {
                @Override
                public void onSuccess() {
                    Log.d("MainActivity", "access_token is valid");
                    openDevicesActivity();
                    finish();
                }

                @Override
                public void onFailure(failureType type, String message) {
                    if (type == failureType.CANNOT_REACH_SERVER) {
                        showErrorDialogue(message);
                    } else if (type == failureType.INVALID_TOKEN) {
                        getAccessToken();
                    } else if (type == failureType.SERVER_ERROR) {
                        showErrorDialogue("Opps! There is a server error!");
                    } else {
                        showErrorDialogue("Something went wrong!");
                    }
                }
            });
        } else {
            Log.d("MainActivity", "access_token is null");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    openOnboardActivity();
                }
            }, 2500);
        }
    }

    void getAccessToken() {
        Log.d("MainActivity", "getAccessToken");
        Context context = getApplicationContext();
        SharedPreferences userAccessPreference = context.getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme(BuildConfig.API_SERVER_SCHEME)
                .encodedAuthority(BuildConfig.API_SERVER_HOST)
                .appendPath("oauth")
                .appendPath("token")
                .build();

        Map<String, String> params = new HashMap<>();
        params.put("client_id", BuildConfig.API_SERVER_CLIENT_ID);
        params.put("client_secret", BuildConfig.API_SERVER_CLIENT_SECRET);
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", userAccessPreference.getString("refresh_token", null));

        RequestQueue queue = Volley.newRequestQueue(this);
        Log.d("MainActivity", "make request to the sever");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri.toString(), new JSONObject(params), (JSONObject response) -> {
            Log.d("MainActivity", "got response from the sever");
            try {
                String access_token = response.getString("access_token");
                String refresh_token = response.getString("refresh_token");
                long accessTokenCreatedAt = response.getLong("created_at");
                int accessTokenExpiresIn = response.getInt("expires_in");
                userAccessPreference.edit().putString("access_token", access_token).apply();
                userAccessPreference.edit().putString("refresh_token", refresh_token).apply();
                userAccessPreference.edit().putLong("access_token_created_at", accessTokenCreatedAt).apply();
                userAccessPreference.edit().putInt("access_token_expires_in", accessTokenExpiresIn).apply();
                openDevicesActivity();
                finish();
            } catch (Exception e) {
                String message = e.getMessage();
                Log.d("MainActivity", "exception occurred!");
                showErrorDialogue(message);
            }
        }, (VolleyError error) -> {
            // access token and request token are not valid anymore
            // ask the user to login
            openOnboardActivity();
        });
        queue.add(jsonObjectRequest);
    }

    void showErrorDialogue(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error!")
                .setMessage(error)
                .setPositiveButton("OK", null)
                .show();
    }
}
