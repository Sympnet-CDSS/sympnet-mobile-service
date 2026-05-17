package com.sympnet.app.activities.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.app.R;
import com.sympnet.app.home.ActivityHome;
import java.util.Locale;

public class LanguageActivity extends AppCompatActivity {

    private static final String PREFS = "SympNetPrefs";
    private static final String KEY_LANG = "appLanguage";

    private LinearLayout btnEnglish, btnFrench, btnArabic;
    private TextView tvCheckEn, tvCheckFr, tvCheckAr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnEnglish = findViewById(R.id.btnEnglish);
        btnFrench  = findViewById(R.id.btnFrench);
        btnArabic  = findViewById(R.id.btnArabic);
        tvCheckEn  = findViewById(R.id.tvCheckEn);
        tvCheckFr  = findViewById(R.id.tvCheckFr);
        tvCheckAr  = findViewById(R.id.tvCheckAr);

        // Afficher la langue actuelle
        String current = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_LANG, "en");
        updateChecks(current);

        btnEnglish.setOnClickListener(v -> applyLanguage("en"));
        btnFrench.setOnClickListener(v  -> applyLanguage("fr"));
        btnArabic.setOnClickListener(v  -> applyLanguage("ar"));
    }

    private void applyLanguage(String langCode) {
        // Sauvegarder
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_LANG, langCode)
                .apply();

        // Appliquer
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());

        // Redémarrer l'app depuis ActivityHome
        Intent intent = new Intent(this, ActivityHome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateChecks(String langCode) {
        tvCheckEn.setVisibility("en".equals(langCode) ?
                android.view.View.VISIBLE : android.view.View.GONE);
        tvCheckFr.setVisibility("fr".equals(langCode) ?
                android.view.View.VISIBLE : android.view.View.GONE);
        tvCheckAr.setVisibility("ar".equals(langCode) ?
                android.view.View.VISIBLE : android.view.View.GONE);
    }
}