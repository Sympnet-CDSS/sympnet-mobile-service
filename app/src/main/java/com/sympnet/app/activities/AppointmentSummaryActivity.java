package com.sympnet.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.app.R;
import com.sympnet.app.home.ActivityHome;

public class AppointmentSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_summary);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnDone = findViewById(R.id.btnDone);
        RatingBar ratingBar = findViewById(R.id.ratingBar);

        btnBack.setOnClickListener(v -> finish());

        ratingBar.setOnRatingBarChangeListener((rb, rating, fromUser) -> {
            Toast.makeText(this, "Thank you for rating: " + rating + " stars!", Toast.LENGTH_SHORT).show();
        });

        btnDone.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityHome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
