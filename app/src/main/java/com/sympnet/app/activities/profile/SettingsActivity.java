package com.sympnet.app.activities.profile;

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

        setupMenuItem(findViewById(R.id.menuNotification), "Paramètres de notifications", R.drawable.ic_notifications);
        setupMenuItem(findViewById(R.id.menuPasswordManager), "Gestionnaire de mot de passe", android.R.drawable.ic_lock_idle_lock);
        setupMenuItem(findViewById(R.id.menuDeleteAccount), "Supprimer le compte", android.R.drawable.ic_menu_delete);
        setupMenuItem(
                findViewById(R.id.menuLanguage),
                "Langue",
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

        setupMenuItem(findViewById(R.id.menuHelp), "Aide & Support", R.drawable.ic_help);
        setupMenuItem(findViewById(R.id.menuPrivacy), "Politique de confidentialité", R.drawable.ic_security);

        findViewById(R.id.menuHelp).setOnClickListener(v -> {
            startActivity(new Intent(this, HelpActivity.class));
        });

        findViewById(R.id.menuPrivacy).setOnClickListener(v -> {
            startActivity(new Intent(this, PrivacyPolicyActivity.class));
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
