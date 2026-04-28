package com.sympnet.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.app.R;

public class BookingActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_DATE = "extra_selected_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        btnBack.setOnClickListener(v -> finish());

        btnConfirmBooking.setOnClickListener(v -> {
            startActivity(new Intent(this, AppointmentSummaryActivity.class));
        });
    }
}
