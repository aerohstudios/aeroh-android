package io.aeroh.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                    .appendQueryParameter("scope", "basic_info read_devices write_devices control_devices").build();

            Intent login_intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(login_intent);
        });
    }
}
