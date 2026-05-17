package com.sympnet.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.app.R;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnChatSupport = findViewById(R.id.btnChatSupport);
        Button btnEmailSupport = findViewById(R.id.btnEmailSupport);
        Button btnPhoneSupport = findViewById(R.id.btnPhoneSupport);
        TextView tvViewAllFaq = findViewById(R.id.tvViewAllFaq);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnChatSupport != null) {
            btnChatSupport.setOnClickListener(v -> {
                Toast.makeText(this, "Chat en direct...", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnEmailSupport != null) {
            btnEmailSupport.setOnClickListener(v -> {
                Toast.makeText(this, "Envoi d'email...", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnPhoneSupport != null) {
            btnPhoneSupport.setOnClickListener(v -> {
                Toast.makeText(this, "Appel au support...", Toast.LENGTH_SHORT).show();
            });
        }

        if (tvViewAllFaq != null) {
            tvViewAllFaq.setOnClickListener(v -> {
                Toast.makeText(this, "Toutes les questions...", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
