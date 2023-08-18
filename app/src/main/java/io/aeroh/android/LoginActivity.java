package io.aeroh.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.aeroh.android.utils.HelperMethods;
import io.aeroh.android.utils.NetworkStatus;

public class LoginActivity extends AppCompatActivity {

    private static final int Delay = 500;
    private static final String SCOPE_MOBILE = "mobile";
    private static final int animationDelay = 3500;
    private RequestQueue requestQueue;
    private Animation fadeInAnimation;
    private Animation fadeOutAnimation;
    Button loginButton, signUpButton;
    EditText emailField, passwordField;
    TextView emailError, passwordError, warningMessage, forgotPassword;
    LinearLayout loginNetworkError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = findViewById(R.id.login_button);
        signUpButton = findViewById(R.id.signupbtn);
        emailField = findViewById(R.id.userEmail);
        passwordField = findViewById(R.id.userPassword);
        emailError = findViewById(R.id.loginEmailError);
        passwordError = findViewById(R.id.loginPasswordError);
        loginNetworkError = findViewById(R.id.loginNetworkError);
        warningMessage = findViewById(R.id.loginWarningMessage);
        forgotPassword = findViewById(R.id.forgotPassword);

        // Initialize the RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkStatus.isInternetConnected(getApplicationContext())) {
                    showNetworkError(getResources().getString(R.string.internet_error));
                } else {
                    resetErrors();
                    String userEmail = emailField.getText().toString();
                    String userPassword = passwordField.getText().toString();
                    long timestamps = System.currentTimeMillis() / 1000;
                    String hmacSignature = HelperMethods.GenLoginPayloadSignature(userEmail, userPassword, timestamps);
                    if (!userEmail.isEmpty() && !userPassword.isEmpty()) {
                        resetErrors();
                        String clientId = BuildConfig.API_SERVER_CLIENT_ID;
                        makeLoginApiCall(userEmail, userPassword, timestamps, clientId, hmacSignature);
                    } else {
                        if (userEmail.isEmpty()) {
                            showFieldErrors(emailError, emailField, getResources().getString(R.string.empty_email_error));
                        } else {
                            resetErrors();
                        }
                        if (userPassword.isEmpty()) {
                            showFieldErrors(passwordError, passwordField, getResources().getString(R.string.empty_password_error));
                        } else {
                            resetErrors();
                        }
                    }
                }
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignup();
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                openForgotPassword();
            }
        });
    }

    //Open Forgot Password
    private void openForgotPassword() {
        Animation clickAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_animation);
        forgotPassword.startAnimation(clickAnim);
        Intent httpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.API_SERVER_SCHEME + "://" + BuildConfig.API_SERVER_HOST + "/users/password/new"));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(httpIntent);
                finish();
            }
        }, animationDelay);
    }

    //open Signup Activity
    private void openSignup() {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
        finish();
    }

    //Reset errors on Login page
    private void resetErrors() {
        emailError.setVisibility(View.INVISIBLE);
        emailField.setBackgroundResource(R.drawable.background_with_stroke_white);
        passwordError.setVisibility(View.INVISIBLE);
        passwordField.setBackgroundResource(R.drawable.background_with_stroke_white);
        warningMessage.setText(R.string.internet_error);
    }

    //Handle Network Error
    private void showNetworkError(String errorMessage) {
        fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        fadeOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        loginNetworkError.setVisibility(View.VISIBLE);
        loginNetworkError.setAnimation(fadeInAnimation);
        warningMessage.setText(errorMessage);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loginNetworkError.setAnimation(fadeOutAnimation);
                loginNetworkError.setVisibility(View.INVISIBLE);
            }
        }, animationDelay);
    }

    //handle text field errors
    private void showFieldErrors(TextView textView, EditText editText, String errorMessage) {
        textView.setText(errorMessage);
        textView.setVisibility(View.VISIBLE);
        editText.setBackgroundResource(R.drawable.error_background);
    }

    //login api call
    private void makeLoginApiCall(String Email, String Password, Long timestamp, String clientId, String signature) {
        // API endpoint
        String url = BuildConfig.API_SERVER_SCHEME + "://" + BuildConfig.API_SERVER_HOST + "/users/sign_in";

        // Creating Request Body as a JSON string
        JSONObject json = new JSONObject();
        try {
            json.put("email", Email);
            json.put("password", Password);
            json.put("scopes", SCOPE_MOBILE);
            json.put("timestamp", timestamp);
            json.put("client_id", clientId);
            json.put("signature", signature);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String loginRequestBody = json.toString();

        // Creating the StringRequest
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Message", response);
                        try {
                            //Handling JSON Response
                            JSONObject serverResponse = new JSONObject(response);
                            JSONObject userData = new JSONObject(serverResponse.getString("data"));
                            String accessToken = userData.getString("access_token");
                            String refreshToken = userData.getString("refresh_token");
                            long accessTokenCreatedAt = userData.getLong("created_at");
                            int accessTokenExpiresIn = userData.getInt("expires_in");

                            //adding the user access data to the shared preferences
                            SharedPreferences user_access_preferences = getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = user_access_preferences.edit();
                            editor.putString("access_token", accessToken);
                            editor.putString("refresh_token", refreshToken);
                            editor.putLong("access_token_created_at", accessTokenCreatedAt);
                            editor.putInt("access_token_expires_in", accessTokenExpiresIn);
                            editor.apply();

                            //Showing the Devices Activity
                            new Handler().postDelayed(new Runnable() {
                                final Intent intent = new Intent(getApplicationContext(), DevicesActivity.class);

                                @Override
                                public void run() {
                                    startActivity(intent);
                                    finish();
                                }
                            }, Delay);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            //Handling error response
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                    // Handle network error
                    showNetworkError("Bad Network!");
                } else if (error instanceof TimeoutError || error instanceof ServerError) {
                    // Handle timeout error
                    showNetworkError("Server Timed Out!");
                } else if (error instanceof AuthFailureError) {
                    String responseData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    try {
                        JSONObject response = new JSONObject(responseData);
                        JSONArray errorArray = response.getJSONArray("errors");
                        String errorMessage = errorArray.getString(0);
                        showNetworkError(errorMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    showNetworkError("Something went wrong. Please try again later!");
                }
            }

        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return loginRequestBody.getBytes(StandardCharsets.UTF_8);
            }
        };

        // Adding the request to RequestQueue
        requestQueue.add(loginRequest);
    }
}
