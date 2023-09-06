package io.aeroh.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Properties;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;
import io.aeroh.android.BuildConfig;

public class MQTTClient {
    String clientId = "Aeroh-One-App";
    MqttConnectOptions mqttConnectOptions = null;
    MqttAndroidClient mqttClient = null;

    public enum MQTTClientStatus {
        Disconnected,
        Connecting,
        Connected,
        ConnectionFailed,
    }

    public MQTTClientStatus mqttClientStatus = MQTTClientStatus.Disconnected;

    ArrivedMessageCallback arrivedMessageCallback = null;

    public MQTTClient(Context context, String mqttUriGeneric, String thingName) {
        // Create Web Socket Based MQTT Url
        Uri mqttUri = Uri.parse(mqttUriGeneric);
        String host = mqttUri.getHost();
        String scheme = "wss";
        int port = 443;
        String mqttUriStr = String.format("%s://%s:%d", scheme, host, port);
        Log.d("MQTTClient", "Using MQTT_URI: " + mqttUriStr);

        // Generate Request Headers
        mqttConnectOptions = new MqttConnectOptions();
        Properties properties = new Properties();
        properties.setProperty("X-Amz-CustomAuthorizer-Name", "Aeroh_One_Mobile_App_Authorizer");
        properties.setProperty("X-Aeroh-App-OS", "Android");
        properties.setProperty("X-Aeroh-Android-SDK-Version", String.valueOf(Build.VERSION.SDK_INT));
        properties.setProperty("X-Aeroh-Android-Release", Build.VERSION.RELEASE);
        properties.setProperty("X-Aeroh-App-ID", BuildConfig.APPLICATION_ID);
        properties.setProperty("X-Aeroh-App-Version-Code", String.valueOf(BuildConfig.VERSION_CODE));
        properties.setProperty("X-Aeroh-App-Version-Name", BuildConfig.VERSION_NAME);

        SharedPreferences shared_preferences = context.getSharedPreferences("Aeroh", Context.MODE_PRIVATE);
        String access_token = shared_preferences.getString("access_token", null);
        if (access_token != null) {
            properties.put("X-Aeroh-Oauth2-Access-Token", access_token);
        }

        String[] aerohDevices = new String[] { thingName };
        properties.put("X-Aeroh-Devices", TextUtils.join(",", aerohDevices));

        mqttConnectOptions.setCustomWebSocketHeaders(properties);

        Log.d("MQTTClient", "Setting up MQTT Client");
        mqttClient = new MqttAndroidClient(context, mqttUriStr, clientId, Ack.AUTO_ACK);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d("MQTTClient", "Connection Lost");
                if (cause != null) {
                    Log.d("MQTTClient", "cause: " + cause.getLocalizedMessage());
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("MQTTClient", "Message Arrived");
                Log.d("MQTTClient", "topic: " + topic);
                Log.d("MQTTClient", "message: " + message.toString());
                if (arrivedMessageCallback != null) {
                    arrivedMessageCallback.call(topic, message);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("MQTTClient", "Delivery Complete");
                Log.d("MQTTClient", "token: " + token.toString());
            }
        });

    }

    public interface Callback {
        void onSuccess(IMqttToken asyncActionToken);
        void onFailure(IMqttToken asyncActionToken, Throwable exception);
    }

    public interface ArrivedMessageCallback {
        void call(String topic, MqttMessage message);
    }

    public void connect(Callback callback) {
        Log.d("MQTTClient", "MQTT Client Connect");

        if (mqttClientStatus == MQTTClientStatus.Connecting ||
                mqttClientStatus == MQTTClientStatus.Connected) {
            Log.d("MQTTClient", "Already connected to MQTT Server.");
            return;
        }

        mqttClientStatus = MQTTClientStatus.Connecting;

        mqttClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d("MQTTClient", "connect succeed");
                mqttClientStatus = MQTTClientStatus.Connected;
                callback.onSuccess(asyncActionToken);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.d("MQTTClient", "connect failed");
                mqttClientStatus = MQTTClientStatus.ConnectionFailed;
                exception.printStackTrace();
                callback.onFailure(asyncActionToken, exception);
            }
        });

        Log.d("MQTTClient", "Initiated MQTT Connect with Callback");
    }

    public void publish(String topic, String messageStr, Callback callback) {
        Log.d("MQTTClient", "Publishing to topic: " + topic);
        Log.d("MQTTClient", "with message: " + messageStr);
        MqttMessage message = new MqttMessage();
        message.setPayload(messageStr.getBytes());
        message.setQos(0);
        mqttClient.publish(topic, message, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                // doing additional checks because paho doesn't report failures
                // likely due to this issue reported in the python library:
                // https://github.com/eclipse/paho.mqtt.python/issues/440
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Log.d("MQTTClient", "IsConnected " + String.format("%b at 300ms", mqttClient.isConnected()));
                        if (mqttClient.isConnected()) {

                            Log.d("MQTTClient", "publish succeed!");
                            callback.onSuccess(asyncActionToken);
                        } else {
                            callback.onFailure(asyncActionToken, new Exception("Likely Authorization Error"));
                        }
                    }
                }, 300);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.d("MQTTClient", "publish failed!");
                callback.onFailure(asyncActionToken, exception);
            }
        });
    }

    public void subscribe(String topic, Callback callback, ArrivedMessageCallback arrivedMessageCallback) {
        Log.d("MQTTClient", "Subscribing to topic: " + topic);
        if (arrivedMessageCallback != null) {
            this.arrivedMessageCallback = arrivedMessageCallback;
        }
        mqttClient.subscribe(topic, 1, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d("MQTTClient", "subscribeOnSuccess");
                if (callback != null) {
                    callback.onSuccess(asyncActionToken);
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.d("MQTTClient", "subscribeOnFailure");
                if (callback != null) {
                    callback.onFailure(asyncActionToken, exception);
                }
            }
        });
    }

    public void unsubscribe(String topic) {
        mqttClient.unsubscribe(topic);
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    public void disconnect() {
        mqttClient.disconnect();
        mqttClientStatus = MQTTClientStatus.Disconnected;
        Log.d("MQTTClient", "Disconnected");
    }
}
