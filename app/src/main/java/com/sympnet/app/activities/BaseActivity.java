package com.sympnet.app.activities;

import android.content.Intent;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.app.R;
import com.sympnet.app.home.ActivityHome;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setupBottomNav() {
        ImageView navHome     = findViewById(R.id.nav_home_icon);
        ImageView navChat     = findViewById(R.id.nav_chat_icon);
        ImageView navProfile  = findViewById(R.id.nav_profile_icon);
        ImageView navCalendar = findViewById(R.id.nav_calendar_icon);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, ActivityHome.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            });
        }

        if (navCalendar != null) {
            navCalendar.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("TARGET_FRAGMENT", "SCHEDULE")
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            });
        }

        if (navChat != null) {
            navChat.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("TARGET_FRAGMENT", "CHAT")
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("TARGET_FRAGMENT", "PROFILE")
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            });
        }
    }
}
