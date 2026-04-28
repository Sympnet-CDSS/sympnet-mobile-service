package com.sympnet.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.sympnet.app.R;

public class NotificationSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupSwitch(findViewById(R.id.settingGeneral), "General Notification", true);
        setupSwitch(findViewById(R.id.settingSound), "Sound", true);
        setupSwitch(findViewById(R.id.settingVibrate), "Vibrate", false);
        setupSwitch(findViewById(R.id.settingSpecialOffers), "Special Offers", true);
        setupSwitch(findViewById(R.id.settingPayments), "Payments", false);
        setupSwitch(findViewById(R.id.settingCashback), "Cashback", true);
    }

    private void setupSwitch(View container, String title, boolean isChecked) {
        if (container == null) return;
        TextView tvTitle = container.findViewById(R.id.tvNotificationTitle);
        SwitchCompat sw = container.findViewById(R.id.switchNotification);
        if (tvTitle != null) tvTitle.setText(title);
        if (sw != null) sw.setChecked(isChecked);
    }
}
