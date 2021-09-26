package com.awishkara.druid_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.awishkara.druid_android.api.Users;
import com.awishkara.druid_android.models.User;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.banana.jsonapi2.JsonApiConverterFactory;
import moe.banana.jsonapi2.ResourceAdapterFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences shared_preferences = getApplicationContext().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String access_token = shared_preferences.getString("API_SERVER_ACCESS_TOKEN", null);
        if (access_token != null) {
            String baseUrl = BuildConfig.API_SERVER_SCHEME + "://" + BuildConfig.API_SERVER_HOST + "/api/v1/";
            JsonAdapter.Factory jsonApiAdapterFactory = ResourceAdapterFactory.builder()
                    .add(User.class)
                    .build();
            Moshi moshi = new Moshi.Builder()
                    .add(jsonApiAdapterFactory)
                    .build();
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            okhttp3.Request.Builder requestBuilder = chain.request().newBuilder();
                            requestBuilder.header("Authorization", "Bearer " + access_token);
                            return chain.proceed(requestBuilder.build());
                        }
                    })
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(httpClient)
                    .addConverterFactory(JsonApiConverterFactory.create(moshi))
                    .build();

            Users users = retrofit.create(Users.class);

            Call<List<User>> call = users.list();

            Context context = getApplicationContext();
            call.enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(Call<List<User>> call, retrofit2.Response<List<User>> response) {
                    int statusCode = response.code();
                    if (statusCode == 200) {
                        // if valid take me to devices
                        Toast toast = Toast.makeText(context, "Take me to devices", Toast.LENGTH_SHORT);
                        toast.show();

                    } else if (statusCode == 401) {
                        shared_preferences.edit().remove("API_SERVER_ACCESS_TOKEN").apply();
                    }
                }

                @Override
                public void onFailure(Call<List<User>> call, Throwable t) {
                    // log failure
                }
            });
        }

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
                Uri uri = builder.scheme(BuildConfig.API_SERVER_SCHEME)
                        .encodedAuthority(BuildConfig.API_SERVER_HOST)
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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri.toString(), new JSONObject(params), (JSONObject response) -> {
            try {
                String access_token = response.getString("access_token");
                SharedPreferences shared_preferences = context.getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                shared_preferences.edit().putString("API_SERVER_ACCESS_TOKEN", access_token).apply();
            } catch (Exception e) {

            }
        }, (VolleyError error) -> {

        });

        queue.add(jsonObjectRequest);
    }
}