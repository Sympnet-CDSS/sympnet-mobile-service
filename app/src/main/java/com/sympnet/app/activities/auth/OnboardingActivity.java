package com.sympnet.app.activities.auth;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sympnet.app.R;
import androidx.viewpager2.widget.ViewPager2;
import com.sympnet.app.activities.BaseActivity;
import com.sympnet.app.adapters.OnboardingPagerAdapter;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends BaseActivity {

    // SharedPreferences constants 
    private static final String PREFS_NAME  = "sympnet_prefs";
    private static final String KEY_FIRST   = "onboarding_completed";
    private static final int    PAGE_COUNT  = 4;

    // Views 
    private ViewPager2        viewPager;
    private LinearLayout      dotsLayout;
    private MaterialButton    btnNext;
    private TextView          btnSkip;
    private View[]            dots;

    // Active dot animator
    private ObjectAnimator    activeDotAnim;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // First-launch guard
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_FIRST, false)) {
            // Not the first launch 
            return;
        }

        setContentView(R.layout.activity_onboarding);

        viewPager  = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.dots_layout);
        btnNext    = findViewById(R.id.btn_next);
        btnSkip    = findViewById(R.id.btn_skip);

        // ViewPager setup
        OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Smooth page-fade + scale transition
        viewPager.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);
            page.setAlpha(1f - absPos * 0.4f);
            page.setScaleY(1f - absPos * 0.04f);
            page.setTranslationX(-position * page.getWidth() * 0.08f);
        });

        buildDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                buildDots(position);
                animateDot(position);

                boolean isLast = position == PAGE_COUNT - 1;
                btnNext.setText(isLast ? "COMMENCER" : "SUIVANT →");
                btnSkip.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
            }
        });

        // Button listeners 
        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < PAGE_COUNT - 1) {
                viewPager.setCurrentItem(current + 1, true);
            } else {
                completeOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> completeOnboarding());
    }

   
    // Dots
   
    private void buildDots(int activePosition) {
        dotsLayout.removeAllViews();
        dots = new View[PAGE_COUNT];

        int dp8  = dp(8);
        int dp24 = dp(24);
        int dp6  = dp(6);

        for (int i = 0; i < PAGE_COUNT; i++) {
            View dot = new View(this);

            LinearLayout.LayoutParams lp;
            if (i == activePosition) {
                // Active dot
                lp = new LinearLayout.LayoutParams(dp24, dp8);
                dot.setBackgroundResource(R.drawable.shape_dot_active);
            } else {
                // Inactive
                lp = new LinearLayout.LayoutParams(dp8, dp8);
                dot.setBackgroundResource(R.drawable.shape_dot_inactive);
            }
            lp.setMarginEnd(dp6);
            dot.setLayoutParams(lp);

            dots[i] = dot;
            dotsLayout.addView(dot);
        }
    }

    private void animateDot(int position) {
        if (activeDotAnim != null) activeDotAnim.cancel();
        View dot = dots[position];
        activeDotAnim = ObjectAnimator.ofFloat(dot, "scaleX", 1f, 1.3f, 1f);
        activeDotAnim.setDuration(350);
        activeDotAnim.setInterpolator(new DecelerateInterpolator());
        activeDotAnim.start();
    }

    private void completeOnboarding() {
        // Mark onboarding as done 
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_FIRST, true)
                .apply();
        goToLogin();
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    //  Utility

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}