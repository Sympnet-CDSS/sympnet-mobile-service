package com.sympnet.mobile.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.mobile.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView logoIcon;
    private TextView logoText;
    private LinearLayout dotsContainer;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logoIcon = findViewById(R.id.logo_icon);
        logoText = findViewById(R.id.logo_text);
        dotsContainer = findViewById(R.id.dots_container);

        startAnimations();

        handler.postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            startActivity(intent);
            finish();
        }, 2500);
    }

    private void startAnimations() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoIcon, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoIcon, "scaleY", 0f, 1.2f, 1f);
        scaleX.setDuration(600);
        scaleY.setDuration(600);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logoText, "alpha", 0f, 1f);
        fadeIn.setDuration(800);
        fadeIn.setStartDelay(300);

        startDotsAnimation();

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, fadeIn);
        animatorSet.start();
    }

    private void startDotsAnimation() {
        for (int i = 0; i < 3; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                ImageView dot = new ImageView(this);
                dot.setImageDrawable(getDrawable(R.drawable.dot_loading));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(40, 40);
                params.setMargins(10, 0, 10, 0);
                dot.setLayoutParams(params);
                dotsContainer.addView(dot);

                ObjectAnimator pulse = ObjectAnimator.ofFloat(dot, "alpha", 0.3f, 1f, 0.3f);
                pulse.setDuration(800);
                pulse.setRepeatCount(ObjectAnimator.INFINITE);
                pulse.start();
            }, i * 300);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}