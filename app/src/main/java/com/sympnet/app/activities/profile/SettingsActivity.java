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
            new android.app.AlertDialog.Builder(this)
                .setTitle("Supprimer le compte")
                .setMessage("Êtes-vous sûr de vouloir supprimer définitivement votre compte ? Cette action est irréversible.")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    android.content.SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
                    String token = "Bearer " + prefs.getString("userToken", "");
                    String userId = prefs.getString("userId", "");
                    
                    com.sympnet.app.network.ApiClient.getClient().create(com.sympnet.app.network.ApiService.class)
                        .deletePatientAccount(token, userId).enqueue(new retrofit2.Callback<Void>() {
                            @Override
                            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                                if(response.isSuccessful()) {
                                    prefs.edit().clear().apply();
                                    Intent intent = new Intent(SettingsActivity.this, com.sympnet.app.activities.SplashActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(SettingsActivity.this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                                Toast.makeText(SettingsActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                            }
                        });
                })
                .setNegativeButton("Annuler", null)
                .show();
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
