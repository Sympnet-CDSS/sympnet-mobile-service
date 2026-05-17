package com.sympnet.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.sympnet.app.R;
import com.sympnet.app.fragments.AppointmentsFragment;
import com.sympnet.app.fragments.ChatListFragment;
import com.sympnet.app.fragments.ChatbotFragment;
import com.sympnet.app.fragments.ProfileFragment;
import com.sympnet.app.home.ActivityHome;

public class MainActivity extends BaseActivity {

    private ImageView navHome, navChat, navAi, navProfile, navCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navHome     = findViewById(R.id.nav_home_icon);
        navChat     = findViewById(R.id.nav_chat_icon);
        navAi       = findViewById(R.id.nav_ai_icon);
        navProfile  = findViewById(R.id.nav_profile_icon);
        navCalendar = findViewById(R.id.nav_calendar_icon);

        setupBottomNav();
        handleIntentExtras();
    }

    @Override
    protected void setupBottomNav() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, ActivityHome.class));
                finish();
            });
        }

        if (navChat != null) {
            navChat.setOnClickListener(v -> {
                loadFragment(new ChatListFragment());
                updateNavIcons(navChat);
            });
        }

        if (navAi != null) {
            navAi.setOnClickListener(v -> {
                loadFragment(new ChatbotFragment());
                updateNavIcons(navAi);
            });
        }

        if (navCalendar != null) {
            navCalendar.setOnClickListener(v -> {
                loadFragment(new AppointmentsFragment());
                updateNavIcons(navCalendar);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                loadFragment(new ProfileFragment());
                updateNavIcons(navProfile);
            });
        }
    }

    private void updateNavIcons(ImageView selectedIcon) {
        int activeColor   = ContextCompat.getColor(this, R.color.white);
        int inactiveColor = Color.parseColor("#B2DFDB");

        navHome.setColorFilter(inactiveColor);
        navChat.setColorFilter(inactiveColor);
        navAi.setColorFilter(inactiveColor);
        navProfile.setColorFilter(inactiveColor);
        navCalendar.setColorFilter(inactiveColor);

        selectedIcon.setColorFilter(activeColor);
    }

    private void handleIntentExtras() {
        String target = getIntent().getStringExtra("TARGET_FRAGMENT");
        if (target != null) {
            switch (target) {
                case "CHAT":
                    loadFragment(new ChatListFragment());
                    updateNavIcons(navChat);
                    break;
                case "CHATBOT":
                    loadFragment(new ChatbotFragment());
                    updateNavIcons(navAi);
                    break;
                case "SCHEDULE":
                    loadFragment(new AppointmentsFragment());
                    updateNavIcons(navCalendar);
                    break;
                case "PROFILE":
                    loadFragment(new ProfileFragment());
                    updateNavIcons(navProfile);
                    break;
                default:
                    // Default to Home or previous state if unknown
                    break;
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}