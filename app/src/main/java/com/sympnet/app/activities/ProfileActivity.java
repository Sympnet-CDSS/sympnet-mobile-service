package com.sympnet.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.sympnet.app.R;

public class ProfileActivity extends AppCompatActivity {

    LinearLayout settingsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        settingsBtn = findViewById(R.id.settingsBtn);

        settingsBtn.setOnClickListener(v -> {
            Intent i = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(i);
        });
    }
}
