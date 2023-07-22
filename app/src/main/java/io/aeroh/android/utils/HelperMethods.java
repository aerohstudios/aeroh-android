package io.aeroh.android.utils;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.aeroh.android.BuildConfig;

public class HelperMethods {

    private static final String SCOPE_MOBILE = "mobile";

    public static String GenSignUpPayloadSignature(String email, String password, String firstName, long timestamps) {
        String delimiter = "|";
        String payloadString = email + delimiter + password + delimiter + firstName + delimiter + SCOPE_MOBILE + delimiter + timestamps;
        Log.d("Payload", payloadString);

        try {
            String algorithm = "HmacSHA256";
            String secret = BuildConfig.API_SERVER_CLIENT_SECRET;

            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm);
            Mac hmacSHA256 = Mac.getInstance(algorithm);
            hmacSHA256.init(keySpec);

            byte[] signatureBytes = hmacSHA256.doFinal(payloadString.getBytes(StandardCharsets.UTF_8));

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

    public static String GenLoginPayloadSignature(String email, String password, long timestamp) {
        String delimiter = "|";
        String payloadString = email + delimiter + password + delimiter + SCOPE_MOBILE + delimiter + timestamp;
        try {
            // Specified the HMAC Algorithm
            String algorithm = "HmacSHA256";

            // Use your own secret key
            String secretKey = BuildConfig.API_SERVER_CLIENT_SECRET;

            // Creating the HMAC SHA256 Object with the secret key
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
            Mac hmacSHA256 = Mac.getInstance(algorithm);
            hmacSHA256.init(keySpec);

            // Generating HMAC Signature
            byte[] signatureBytes = hmacSHA256.doFinal(payloadString.getBytes(StandardCharsets.UTF_8));

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
}
