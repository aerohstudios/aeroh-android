package com.awishkara.druid_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            String code = uri.getQueryParameter("code");

            getAccessToken(code);
        }

        Button loginBtn = (Button) findViewById(R.id.login);
        loginBtn.setOnClickListener((View view) -> {
                Uri.Builder builder = new Uri.Builder();
                Uri uri = builder.scheme("https")
                        .authority(BuildConfig.API_SERVER_HOST)
                        .appendPath("oauth")
                        .appendPath("authorize")
                        .appendQueryParameter("client_id", BuildConfig.API_SERVER_CLIENT_ID)
                        .appendQueryParameter("redirect_uri", BuildConfig.API_SERVER_REDIRECT_URI)
                        .appendQueryParameter("response_type", "code")
                        .appendQueryParameter("scope", "mobile").build();

                Intent login_intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(login_intent);
        });
    }

    void getAccessToken(String code) {
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme("https")
                .authority("druid-web.herokuapp.com")
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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri.toString(), new JSONObject(params), (JSONObject response) -> {
            try {
                Toast toast = Toast.makeText(context, response.getString("access_token"), Toast.LENGTH_SHORT);
                toast.show();
            } catch (Exception e) {

            }
        }, (VolleyError error) -> {

        });

        queue.add(jsonObjectRequest);
    }
}