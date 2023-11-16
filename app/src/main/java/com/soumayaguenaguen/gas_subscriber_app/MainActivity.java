package com.soumayaguenaguen.gas_subscriber_app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import androidx.core.app.NotificationCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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


    String clientId;
    private String subscriptionTopic; //we kept both variables empty to add the value received from the SharedPreferences later


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // i am Retrieve data from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String brokerAddress = preferences.getString("brokerAddress", "");
        String port = preferences.getString("port", "");
        String topic = preferences.getString("topic", "");
        // i am using this data received from the SharedPreferences to initialize MqttService and subscribe to the topic


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channelId", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel); //this code creates the channel we will be uing for the notfications
        }

        maintext = findViewById(R.id.maintext);
        handler = new Handler(Looper.getMainLooper());
        final String serverUri = "tcp://" + brokerAddress + ":" + port; //i am assigning the received value from the SharedPreferences to the serverUri
        subscriptionTopic = topic; //this were i assigned the topic value received from the SharedPreferences to the topic variable

        // Initialize MQTT client
        clientId = clientId + System.currentTimeMillis(); // i am using this to ensure that each time we connect to broker we use unique id
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId); //in here we give the mqtt the variable it will be using to connect to the requested broker

        // Set MQTT callbacks
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    subscribeToTopic();// the function i  will be using to subscribe to the topic i want or the user wants
                }
                updateConnectionStatus("Connected to broker");
            }

            @Override
            public void connectionLost(Throwable cause) {
                //this is in case we lose connection to the broker
                updateConnectionStatus("Connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // using this function we are going to update the text value in the ui to display the most recent value every 5 seconds
                updateUIWithLatestMessage(new String(message.getPayload()));

                // once we receive a new value we notify the user by launching the showNotification function
                showNotification("Gas Value Changed", "New gas value: " + new String(message.getPayload()));
            }

            private void showNotification(String title, String message) {
                //this function is used to build and show a notification with the new value and the title and message provided in the messageArrived function
                Log.d("Notification", "Building notification. Title: " + title + ", Message: " + message); //i used this to check whether a notification is being built or not, to fix error regarding the notfication not being displayed
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(MainActivity.this, "channelId")
                                .setSmallIcon(R.drawable.ic_baseline_fire_extinguisher_24) //used to set the small icon on the top-left side
                                .setContentTitle(title) //this is where the title that was put in the messageArrived is added
                                .setContentText(message) //this is the message
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT); //setts the importance in showing the notification in the notification bar

                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent); //related to the action when the use clicks on the notification

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, builder.build()); //this is where we build and add the notification id
            }



            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Handle message delivery complete
            }
        });


        // This function is used to connect to MQTT broker
        connectToBroker();

        // this part is supposed to schedule a task to update the UI every 5 seconds
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
        mqttConnectOptions.setCleanSession(false); //this is where  we connect to the broker

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
            IMqttToken token = mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() { //this is the part we use to connect to the topic
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    updateConnectionStatus("Subscribed to topic"); //shows this message if it succeeds in subscribing to th topic
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Handle subscription failure if needed
                    updateConnectionStatus("Failed to subscribe to topic"); //in case it failed it shows this message
                }
            });

            // Set a callback for when the subscription is complete
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
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
            } //this is where we update the ui with the new value
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
