package com.soumayaguenaguen.gas_subscriber_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;


public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        EditText brokerAddressEditText = findViewById(R.id.brokerAddressEditText);
        EditText portEditText = findViewById(R.id.portEditText);
        EditText topicEditText = findViewById(R.id.topicEditText);
        Button submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {
            String brokerAddress = brokerAddressEditText.getText().toString();
            String port = portEditText.getText().toString();
            String topic = topicEditText.getText().toString(); //getting the user input

            // this will Save the user data to SharedPreferences
            SharedPreferences preferences = getSharedPreferences("UserData", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("brokerAddress", brokerAddress);
            editor.putString("port", port);
            editor.putString("topic", topic);
            editor.apply();
            Log.d("registered userdata", "data was saved succesfully. Broker: " + brokerAddress + ", Port: " + port + "Topic: " + topic);
            //i used this to check whether a data is being saved or not

            // it will Start MainActivity only after it had saved the data
            Intent intent = new Intent(UserActivity.this, MainActivity.class);
            startActivity(intent);
        });

    }
    }
