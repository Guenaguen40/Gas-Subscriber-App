package com.soumayaguenaguen.gas_subscriber_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
                String topic = topicEditText.getText().toString();


            Intent intent = new Intent(UserActivity.this, MainActivity.class);
            intent.putExtra("brokerAddress", brokerAddress);
            intent.putExtra("port", port);
            intent.putExtra("topic", topic);
            startActivity(intent);
            });
        }
    }
