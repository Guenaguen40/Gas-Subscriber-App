package com.soumayaguenaguen.gas_subscriber_app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import androidx.core.app.NotificationCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {
    private TextView maintext;
    private Handler handler;
    private MqttAndroidClient mqttAndroidClient;


    String clientId = "soumaya654635135abroker";
    private String subscriptionTopic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Retrieve data from Intent
        String brokerAddress = getIntent().getStringExtra("brokerAddress");
        String port = getIntent().getStringExtra("port");
        String topic = getIntent().getStringExtra("topic");
        String threshold = getIntent().getStringExtra("threshold");
        // Use the data as needed, e.g., initialize MqttService and subscribe to the topic


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channelId", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        maintext = findViewById(R.id.maintext);
        handler = new Handler(Looper.getMainLooper());
        final String serverUri = "tcp://" + brokerAddress + ":" + port;
        subscriptionTopic = topic;

        // Initialize MQTT client
        clientId = clientId + System.currentTimeMillis();
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);

        // Set MQTT callbacks
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    subscribeToTopic();
                }
                updateConnectionStatus("Connected to broker");
            }

            @Override
            public void connectionLost(Throwable cause) {
                // Handle connection lost
                updateConnectionStatus("Connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // Handle incoming message
                updateUIWithLatestMessage(new String(message.getPayload()));

                // Inside your code where the new value is received
                showNotification("Gas Value Changed", "New gas value: " + new String(message.getPayload()));
            }

            private void showNotification(String title, String message) {
                Log.d("Notification", "Building notification. Title: " + title + ", Message: " + message);
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(MainActivity.this, "channelId")
                                .setSmallIcon(R.drawable.ic_baseline_fire_extinguisher_24)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, builder.build());
            }



            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Handle message delivery complete
            }
        });


        // Connect to MQTT broker
        connectToBroker();

        // Schedule a task to update UI every 5 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Reschedule the task
                handler.postDelayed(this, 5000); // 5000 milliseconds = 5 seconds
            }
        }, 5000); // Initial delay of 5 seconds
    }






    private void connectToBroker() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Handle connection failure
                    // Handle connection failure
                    updateConnectionStatus("Failed to connect to broker: " + exception.getMessage());
                }
            });
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        try {
            IMqttToken token = mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Handle subscription success if needed
                    updateConnectionStatus("Subscribed to topic");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Handle subscription failure if needed
                    updateConnectionStatus("Failed to subscribe to topic");
                }
            });

            // Set a callback for when the subscription is complete
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Subscription completed successfully, now handle incoming messages
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Handle subscription failure if needed
                    updateConnectionStatus("Failed to subscribe to topic");
                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    private void updateUIWithLatestMessage(final String latestMessage) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                maintext.setText(latestMessage);
            }
        });
    }

    private void updateConnectionStatus(final String status) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                maintext.append("\nConnection Status: " + status);
            }
        });


    }
}
