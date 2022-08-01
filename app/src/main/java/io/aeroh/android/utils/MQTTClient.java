package io.aeroh.android.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Properties;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;
import io.aeroh.android.BuildConfig;

public class MQTTClient {
    String clientId = "Aeroh-One-App";
    MqttConnectOptions mqttConnectOptions = null;
    MqttAndroidClient mqttClient = null;

    public MQTTClient(Context context, String mqttUri, HashMap<String, String> headers) {
        Log.d("USING MQTT_URI", mqttUri);

        mqttConnectOptions = new MqttConnectOptions();
        Properties properties = new Properties();
        properties.setProperty("X-Amz-CustomAuthorizer-Name", "Aeroh_One_Mobile_App_Authorizer");
        properties.setProperty("X-Aeroh-App-OS", "Android");
        properties.setProperty("X-Aeroh-Android-SDK-Version", String.valueOf(Build.VERSION.SDK_INT));
        properties.setProperty("X-Aeroh-Android-Release", Build.VERSION.RELEASE);
        properties.setProperty("X-Aeroh-App-ID", BuildConfig.APPLICATION_ID);
        properties.setProperty("X-Aeroh-App-Version-Code", String.valueOf(BuildConfig.VERSION_CODE));
        properties.setProperty("X-Aeroh-App-Version-Name", BuildConfig.VERSION_NAME);

        for (String key : headers.keySet()) {
            String value = headers.get(key);
            properties.setProperty(key, value);
        }

        mqttConnectOptions.setCustomWebSocketHeaders(properties);

        Log.i("MQTT Client", "Setting up MQTT Client");
        mqttClient = new MqttAndroidClient(context, mqttUri, clientId, Ack.AUTO_ACK);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i("MQTT Client", "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i("MQTT Client", "topic: " + topic + ", msg: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i("MQTT Client", "msg delivered");
            }
        });

    }

    public interface Callback {
        void onSuccess();
        void onFailure();
    }

    public void connect(Callback callback) {
        Log.i("MQTT Client", "MQTT Client Connect");
        mqttClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i("MQTT Client", "connect succeed");
                callback.onSuccess();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i("MQTT Client", "connect failed");
                exception.printStackTrace();
                callback.onFailure();
            }
        });

        Log.i("MQTT Client", "Initiated MQTT Connect with Callback");
    }

    public void publish(String topic, String messageStr) {
        MqttMessage message = new MqttMessage();
        message.setPayload(messageStr.getBytes());
        message.setQos(0);
        mqttClient.publish(topic, message, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i("MQTT Client", "publish succeed!");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i("MQTT Client", "publish failed!");
            }
        });
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }
}
