package com.sympnet.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.app.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupMenuItem(findViewById(R.id.menuNotification), "Notification Setting", android.R.drawable.ic_lock_idle_alarm);
        setupMenuItem(findViewById(R.id.menuPasswordManager), "Password Manager", android.R.drawable.ic_lock_idle_lock);
        setupMenuItem(findViewById(R.id.menuDeleteAccount), "Delete Account", android.R.drawable.ic_menu_delete);
        setupMenuItem(
                findViewById(R.id.menuLanguage),
                "Language",
                android.R.drawable.ic_menu_compass
        );

        findViewById(R.id.menuLanguage).setOnClickListener(v ->
                startActivity(new Intent(this, LanguageActivity.class)));

        findViewById(R.id.menuNotification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationSettingsActivity.class));
        });

        findViewById(R.id.menuPasswordManager).setOnClickListener(v -> {
            startActivity(new Intent(this, PasswordManagerActivity.class));
        });

        findViewById(R.id.menuDeleteAccount).setOnClickListener(v -> {
            Toast.makeText(this, "Delete Account functionality", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupMenuItem(View container, String title, int iconRes) {
        if (container == null) return;
        TextView tvTitle = container.findViewById(R.id.tvMenuTitle);
        ImageView ivIcon = container.findViewById(R.id.ivMenuIcon);
        if (tvTitle != null) tvTitle.setText(title);
        if (ivIcon != null) ivIcon.setImageResource(iconRes);
    }
}
