package com.sympnet.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.app.R;
import com.sympnet.app.home.ActivityHome;

public class SplashActivity extends AppCompatActivity {

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.postDelayed(() -> {
            checkNavigation();
        }, 2500);
    }

    private void checkNavigation() {
        SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        boolean isFirstTime = prefs.getBoolean("isFirstTime", true);

        Intent intent;
        if (isFirstTime) {
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            prefs.edit().putBoolean("isFirstTime", false).apply();
        } else if (isLoggedIn) {
            intent = new Intent(SplashActivity.this, ActivityHome.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
