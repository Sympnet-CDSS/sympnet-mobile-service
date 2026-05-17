package com.sympnet.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS_NAME = "SympNetPrefs";
    private static SessionManager instance;
    private SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public String getCurrentUserId() {
        return prefs.getString("userId", "");
    }

    public String getCurrentUserName() {
        return prefs.getString("userName", "");
    }

    public String getUserToken() {
        return prefs.getString("userToken", "");
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("isLoggedIn", false);
    }
    
    public void logout() {
        prefs.edit().clear().apply();
    }
}