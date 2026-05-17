package com.sympnet.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS = "SympNetPrefs";
    private static final String KEY_LANG = "appLanguage";

    public static Context onAttach(Context context) {
        String lang = getPersistedLanguage(context);
        return setLocale(context, lang);
    }

    public static String getPersistedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANG, "en");
    }

    public static Context setLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }
}
