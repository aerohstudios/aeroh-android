package io.aeroh.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class LoginActivity extends AppCompatActivity {

    private static final int Delay = 500;
    private static final String SCOPE_MOBILE = "mobile";
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button LoginButton = findViewById(R.id.login_flow);
        Button SignupButton = findViewById(R.id.sign_up);
        EditText Email_Field = findViewById(R.id.user_email);
        EditText Password_Field = findViewById(R.id.user_password);

        // Initialize the RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String User_Email = Email_Field.getText().toString();
                String User_Password = Password_Field.getText().toString();
                long timestamps = System.currentTimeMillis() / 1000;
                String hmacSignature = genHmacSignature(User_Email, User_Password, timestamps);

                if (!TextUtils.isEmpty(User_Email) && !TextUtils.isEmpty(User_Password)) {
                    String clientId = BuildConfig.API_SERVER_CLIENT_ID;
                    makeApiCall(User_Email, User_Password, timestamps, clientId, hmacSignature);
                } else {
                    Toast.makeText(LoginActivity.this, "Please fill all details", Toast.LENGTH_SHORT).show();
                }
            }
        });
        SignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

    }

    private String genHmacSignature(String Email, String Password, long timestamp) {
        String delimiter = "|";
        String payloadString = Email + delimiter + Password + delimiter + SCOPE_MOBILE + delimiter + timestamp;
        try {
            // Specified the HMAC Algorithm
            String algorithm = "HmacSHA256";

            // Use your own secret key
            String secretKey = BuildConfig.API_SERVER_CLIENT_SECRET;

            // Creating the HMAC SHA256 Object with the secret key
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
            Mac HmacSHA256 = Mac.getInstance(algorithm);
            HmacSHA256.init(keySpec);

            // Generating HMAC Signature
            byte[] signatureBytes = HmacSHA256.doFinal(payloadString.getBytes(StandardCharsets.UTF_8));

            // Converting the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : signatureBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void makeApiCall(String Email, String Password, Long timestamp, String clientId, String signature) {
        // API endpoint
        String url = BuildConfig.API_LOCAL_HOST + "/users/sign_in";

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

        final String requestBody = json.toString();

        // Creating the StringRequest
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Message", response);
                        try {
                            //
                            JSONObject user_access = new JSONObject(response);
                            String access_token = user_access.getString("access_token");
                            String refresh_token = user_access.getString("refresh_token");
                            long access_token_created_at = user_access.getLong("created_at");
                            int access_token_expires_in = user_access.getInt("expires_in");

                            //adding the user access data to the shared preferences
                            SharedPreferences user_access_preferences = getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = user_access_preferences.edit();
                            editor.putString("access_token", access_token);
                            editor.putString("refresh_token", refresh_token);
                            editor.putLong("access_token_created_at", access_token_created_at);
                            editor.putInt("access_token_expires_in", access_token_expires_in);
                            editor.apply();

                            //Showing the Devices Activity
                            new Handler().postDelayed(new Runnable() {
                                final Intent intent = new Intent(getApplicationContext(), DevicesActivity.class);

                                @Override
                                public void run() {
                                    startActivity(intent);
                                }
                            }, Delay);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    Log.e("SignInResponseError", responseBody);

                } else {
                    Log.e("SignInError", error.toString());
                }
            }

        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return requestBody.getBytes(StandardCharsets.UTF_8);
            }
        };

        // Adding the request to RequestQueue
        requestQueue.add(stringRequest);
    }
}
