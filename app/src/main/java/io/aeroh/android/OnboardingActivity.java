package io.aeroh.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class OnboardingActivity extends AppCompatActivity {

    Button sign_up_button, login_button;
    int delay = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);


        sign_up_button = findViewById(R.id.signupbtn);
        login_button = findViewById(R.id.loginbtn);

        sign_up_button.setOnClickListener(v -> {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }, delay);
        });


        login_button.setOnClickListener(v -> {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }, delay);

        });
    }
}
