package com.sympnet.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import com.sympnet.app.api.RetrofitClient;
import com.sympnet.app.network.ApiClient;
import java.util.Locale;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(this);
        RetrofitClient.init(this);
        applyLanguage();
    }

    private void applyLanguage() {
        SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        String langCode = prefs.getString("appLanguage", "en");
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}