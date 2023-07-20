package io.aeroh.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

public class SignupActivity extends AppCompatActivity {

    private static final String SCOPE_MOBILE = "mobile";
    private static final int Delay = 500;
    RequestQueue requestQueue;
    Button signupButton;
    EditText User_Name, User_Email, User_Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        requestQueue = Volley.newRequestQueue(this);
        signupButton = findViewById(R.id.sign_up_btn);
        User_Name = findViewById(R.id.nameInput);
        User_Email = findViewById(R.id.emailInput);
        User_Password = findViewById(R.id.passwordInput);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name = User_Name.getText().toString();
                String Email = User_Email.getText().toString();
                String Password = User_Password.getText().toString();
                long Timestamps = System.currentTimeMillis() / 1000;
                String ClientId = BuildConfig.API_SERVER_CLIENT_ID;
                String hmacSignature = genHmacSignature(Email, Password, Name, Timestamps);
                Log.d("hmacSignature", hmacSignature);
                makeSignUpApiCall(Name, Email, Password, Timestamps, hmacSignature, ClientId);
            }
        });
    }

    private String genHmacSignature(String Email, String Password, String First_Name, long Timestamps) {

        String Delimiter = "|";
        String PayloadString = Email + Delimiter + Password + Delimiter + First_Name + Delimiter + SCOPE_MOBILE + Delimiter + Timestamps;
        Log.d("Payload", PayloadString);

        try {
            String algorithm = "HmacSHA256";
            String SECRET = BuildConfig.API_SERVER_CLIENT_SECRET;

            SecretKeySpec keySpec = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), algorithm);
            Mac HmacSHA256 = Mac.getInstance(algorithm);
            HmacSHA256.init(keySpec);

            byte[] signatureBytes = HmacSHA256.doFinal(PayloadString.getBytes(StandardCharsets.UTF_8));

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

    private void makeSignUpApiCall(String Name, String Email, String Password, long Timestamp, String Signature, String ClientId) {
        String url = BuildConfig.API_LOCAL_HOST + "/users";

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

        final String requestBody = json.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("SignupResponse", response);
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
                    Log.d("SignupErrorResponse", responseBody);
                } else {
                    Log.d("SignUpError", error.toString());
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
        requestQueue.add(stringRequest);
    }

}
