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
import com.sympnet.app.fragments.ChatFragment;
import com.sympnet.app.fragments.ProfileFragment;
import com.sympnet.app.home.ActivityHome;

public class MainActivity extends AppCompatActivity {

    private ImageView navHome, navChat, navProfile, navCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navHome = findViewById(R.id.nav_home_icon);
        navChat = findViewById(R.id.nav_chat_icon);
        navProfile = findViewById(R.id.nav_profile_icon);
        navCalendar = findViewById(R.id.nav_calendar_icon);

        setupBottomNav();
        handleIntentExtras();
    }

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, ActivityHome.class));
            finish();
        });

        navChat.setOnClickListener(v -> {
            loadFragment(new ChatFragment());
            updateNavIcons(navChat);
        });

        navCalendar.setOnClickListener(v -> {
            loadFragment(new AppointmentsFragment());
            updateNavIcons(navCalendar);
        });

        navProfile.setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
            updateNavIcons(navProfile);
        });
    }

    private void updateNavIcons(ImageView selectedIcon) {
        int activeColor = ContextCompat.getColor(this, R.color.white);
        int inactiveColor = Color.parseColor("#B2DFDB"); // Using the color from the layout

        navHome.setColorFilter(inactiveColor);
        navChat.setColorFilter(inactiveColor);
        navProfile.setColorFilter(inactiveColor);
        navCalendar.setColorFilter(inactiveColor);

        selectedIcon.setColorFilter(activeColor);
    }

    private void handleIntentExtras() {
        String target = getIntent().getStringExtra("TARGET_FRAGMENT");
        if (target != null) {
            if (target.equals("CHAT")) {
                loadFragment(new ChatFragment());
                updateNavIcons(navChat);
            } else if (target.equals("SCHEDULE")) {
                loadFragment(new AppointmentsFragment());
                updateNavIcons(navCalendar);
            } else if (target.equals("PROFILE")) {
                loadFragment(new ProfileFragment());
                updateNavIcons(navProfile);
            }
        } else {
            // If no target, and we are here, it might be an error or default state.
            // But usually we go to ActivityHome.
            loadFragment(new ChatFragment()); // Default for MainActivity now
            updateNavIcons(navChat);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
