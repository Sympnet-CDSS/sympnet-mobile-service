package com.sympnet.app.activities.profile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.sympnet.app.R;

public class NotificationSettingsActivity extends AppCompatActivity {

    private static final String PREFS = "NotificationPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        setupSwitch(R.id.settingGeneral,      " General Notification", "general",      prefs, true);
        setupSwitch(R.id.settingSound,        " Sound",                "sound",        prefs, true);
        setupSwitch(R.id.settingVibrate,      " Vibrate",              "vibrate",      prefs, false);

    }

    private void setupSwitch(int containerId, String title, String key,
                             SharedPreferences prefs, boolean defaultValue) {
        android.view.View container = findViewById(containerId);
        if (container == null) return;

        TextView tvTitle = container.findViewById(R.id.tvNotificationTitle);
        SwitchCompat sw  = container.findViewById(R.id.switchNotification);

        if (tvTitle != null) tvTitle.setText(title);

        if (sw != null) {
            sw.setChecked(prefs.getBoolean(key, defaultValue));
            // Couleur selon état
            updateSwitchColor(sw, sw.isChecked());
            sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(key, isChecked).apply();
                updateSwitchColor(sw, isChecked);
            });
        }
    }

    private void updateSwitchColor(SwitchCompat sw, boolean isChecked) {
        int trackColor  = isChecked ? 0xFF80CBC4 : 0xFFBDBDBD;
        int thumbColor  = isChecked ? 0xFF009688 : 0xFFFFFFFF;
        sw.getTrackDrawable().setTint(trackColor);
        sw.getThumbDrawable().setTint(thumbColor);
    }
}