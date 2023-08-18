package io.aeroh.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
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
import java.util.Arrays;

import io.aeroh.android.utils.HelperMethods;
import io.aeroh.android.utils.NetworkStatus;
import io.aeroh.android.utils.TextValidators;

public class SignupActivity extends AppCompatActivity {

    private static final String SCOPE_MOBILE = "mobile";
    private static final int Delay = 500;
    private static final int animationDelay = 3500;
    RequestQueue requestQueue;
    Button signupButton, loginButton;
    EditText userName, userEmail, userPassword;
    TextView nameError, emailError, passwordError, warningMessage;
    LinearLayout networkError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        requestQueue = Volley.newRequestQueue(this);
        signupButton = findViewById(R.id.sign_up_button);
        loginButton = findViewById(R.id.loginbtn);
        userName = findViewById(R.id.nameInput);
        userEmail = findViewById(R.id.emailInput);
        userPassword = findViewById(R.id.passwordInput);
        nameError = findViewById(R.id.nameError);
        emailError = findViewById(R.id.emailError);
        passwordError = findViewById(R.id.passwordError);
        networkError = findViewById(R.id.networkError);
        warningMessage = findViewById(R.id.warningMessage);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name = userName.getText().toString();
                String Email = userEmail.getText().toString();
                String Password = userPassword.getText().toString();
                long Timestamps = System.currentTimeMillis() / 1000;
                String ClientId = BuildConfig.API_SERVER_CLIENT_ID;
                networkError.setVisibility(View.INVISIBLE);
                warningMessage.setText(R.string.internet_error);
                if (!NetworkStatus.isInternetConnected(getApplicationContext())) {
                    showNetworkError(getResources().getString(R.string.internet_error));
                } else {
                    if (Name.isEmpty() || Email.isEmpty() || Password.isEmpty()) {
                        if (Name.isEmpty()) {
                            showTextAuthenticationError(userName, nameError, getResources().getString(R.string.empty_name_error));
                        } else {
                            resetErrors();
                        }
                        if (Email.isEmpty()) {
                            showTextAuthenticationError(userEmail, emailError, getResources().getString(R.string.empty_email_error));
                        } else {
                            resetErrors();
                        }
                        if (Password.isEmpty()) {
                            showTextAuthenticationError(userPassword, passwordError, getResources().getString(R.string.empty_password_error));
                        } else {
                            resetErrors();
                        }
                    } else {
                        resetErrors();
                        if (!TextValidators.validateEmail(Email) || !TextValidators.validatePassword(Password)) {
                            if (!TextValidators.validateEmail(Email)) {
                                showTextAuthenticationError(userEmail, emailError, getResources().getString(R.string.invalid_format_email));
                            } else {
                                resetErrors();
                            }
                            if (!TextValidators.validatePassword(Password)) {
                                showTextAuthenticationError(userPassword, passwordError, getResources().getString(R.string.invalid_format_password));
                            } else {
                                resetErrors();
                            }
                        } else {
                            resetErrors();
                            String loginHmacSignature = HelperMethods.GenSignUpPayloadSignature(Email, Password, Name, Timestamps);
                            makeSignUpApiCall(Name, Email, Password, Timestamps, loginHmacSignature, ClientId);
                        }
                    }
                }
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginActivity();
            }
        });
    }


    //open login activity
    private void openLoginActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, Delay);
    }

    //reset errors
    private void resetErrors() {
        userName.setBackgroundResource(R.drawable.background_with_stroke_white);
        nameError.setVisibility(View.INVISIBLE);
        userEmail.setBackgroundResource(R.drawable.background_with_stroke_white);
        emailError.setVisibility(View.INVISIBLE);
        userPassword.setBackgroundResource(R.drawable.background_with_stroke_white);
        passwordError.setVisibility(View.INVISIBLE);
    }

    //handle network error
    private void showNetworkError(String errorMessage) {
        resetErrors();
        Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        networkError.setVisibility(View.VISIBLE);
        networkError.setAnimation(fadeInAnimation);
        warningMessage.setText(errorMessage);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                networkError.setAnimation(fadeOutAnimation);
                networkError.setVisibility(View.INVISIBLE);
            }
        }, animationDelay);
    }

    //show email errors
    public void showTextAuthenticationError(EditText editText, TextView textView, String errorMessage) {
        editText.setBackgroundResource(R.drawable.error_background);
        textView.setVisibility(View.VISIBLE);
        textView.setText(errorMessage);
    }



    private void makeSignUpApiCall(String Name, String Email, String Password, long Timestamp, String Signature, String ClientId) {
        String url = BuildConfig.API_SERVER_SCHEME + "://" + BuildConfig.API_SERVER_HOST + "/users";
        JSONObject json = new JSONObject();
        try {
            json.put("email", Email);
            json.put("password", Password);
            json.put("first_name", Name);
            json.put("scopes", SCOPE_MOBILE);
            json.put("timestamp", Timestamp);
            json.put("client_id", ClientId);
            json.put("signature", Signature);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String signUpRequestBody = json.toString();

        StringRequest SignUpRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //Handling JSON Response
                            JSONObject serverResponse = new JSONObject(response);
                            JSONObject userData = new JSONObject(serverResponse.getString("data"));
                            String accessToken = userData.getString("access_token");
                            String refreshToken = userData.getString("refresh_token");
                            long accessTokenCreatedAt = userData.getLong("created_at");
                            int accessTokenExpiresIn = userData.getInt("expires_in");

                            //Adding the user access data to the shared preferences
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
            // Specifying the Payload type
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            // Payload
            public byte[] getBody() {
                return signUpRequestBody.getBytes(StandardCharsets.UTF_8);
            }
        };
        requestQueue.add(SignUpRequest);
    }
}
