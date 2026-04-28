package com.sympnet.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.app.R;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnContactSupport = findViewById(R.id.btnContactSupport);
        Button btnFaq = findViewById(R.id.btnFaq);

        btnBack.setOnClickListener(v -> finish());

        btnContactSupport.setOnClickListener(v -> {
            Toast.makeText(this, "Contacting support...", Toast.LENGTH_SHORT).show();
        });

        btnFaq.setOnClickListener(v -> {
            Toast.makeText(this, "Opening FAQ...", Toast.LENGTH_SHORT).show();
        });
    }
}
