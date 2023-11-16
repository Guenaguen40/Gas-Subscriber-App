package com.soumayaguenaguen.gas_subscriber_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // this Sets the duration of the splash screen in seconds
    private static final int SPLASH_SCREEN_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Replace with your splash screen layout if you have one

        // this is where we tell the app to which interface it should redirect the user to,
        // so if the user has an already saved broker, port and topic it will send him to the main activiy which playes the home role,
        // if not it will send him to the useractivity which will ask him to add the needed info
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Checking if any data is saved in the SharedPreferences file
                if (userDataIsSaved()) {
                    // Data is saved, it will launch MainActivity
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // No data saved, it will launch UserActivity
                    Intent intent = new Intent(SplashActivity.this, UserActivity.class);
                    startActivity(intent);
                }

                // once done it will finish the current activity (splashscreen)
                finish();
            }
        }, SPLASH_SCREEN_DURATION);
    }

    // this is the code where we check if there is any data saved
    private boolean userDataIsSaved() {
        SharedPreferences preferences = getSharedPreferences("UserData", MODE_PRIVATE);
        return preferences.contains("brokerAddress") && preferences.contains("port") && preferences.contains("topic");
    }
}
