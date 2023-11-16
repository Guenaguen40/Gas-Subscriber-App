package com.soumayaguenaguen.gas_subscriber_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import androidx.core.app.NotificationCompat;
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

    final String serverUri = "tcp://broker.hivemq.com:1883";
    String clientId = "soumaya654635135abroker";
    final String subscriptionTopic = "security/gaz";
    private static final String CHANNEL_ID = "channel_id";
    private static final int notificationId = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        maintext = findViewById(R.id.maintext);
        handler = new Handler(Looper.getMainLooper());

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
                showNotification("Test Notification", "This is a test message");
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
    public void showNotification(String title, String message) {
        Log.d("Notification", "Building notification. Title: " + title + ", Message: " + message);
        // Create an intent for the notification click action
        // Create an intent for the notification click action
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

// Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent);

// Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(notificationId, builder.build());

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
