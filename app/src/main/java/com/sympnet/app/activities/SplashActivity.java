package com.sympnet.app.activities;
import com.sympnet.app.activities.auth.OnboardingActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sympnet.app.R;
import com.sympnet.app.activities.auth.LoginActivity;

/**
 * Splash screen — always the launcher activity.
 * Routes to OnboardingActivity on first launch,
 * or directly to LoginActivity on every subsequent launch.
 *
 * In AndroidManifest.xml, set this as the MAIN / LAUNCHER activity.
 */
public class SplashActivity extends BaseActivity {

    private static final String PREFS_NAME = "sympnet_prefs";
    private static final String KEY_FIRST  = "onboarding_completed";
    private static final int    SPLASH_MS  = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Fade-in logo card (containing the new official SympNet logo)
        android.view.View logoCard = findViewById(R.id.splash_logo_card);
        TextView  title = findViewById(R.id.splash_title);

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(700);
        fadeIn.setFillAfter(true);
        logoCard.startAnimation(fadeIn);

        AlphaAnimation fadeInDelayed = new AlphaAnimation(0f, 1f);
        fadeInDelayed.setDuration(700);
        fadeInDelayed.setStartOffset(300);
        fadeInDelayed.setFillAfter(true);
        title.startAnimation(fadeInDelayed);

        // Route after splash delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean completed = prefs.getBoolean(KEY_FIRST, false);

            Class<?> destination = completed ? LoginActivity.class : OnboardingActivity.class;
            startActivity(new Intent(SplashActivity.this, destination));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_MS);
    }
}