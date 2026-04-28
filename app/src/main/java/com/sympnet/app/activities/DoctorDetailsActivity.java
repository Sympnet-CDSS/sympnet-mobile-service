package com.sympnet.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.sympnet.app.R;
import com.sympnet.app.views.Calendarview;

public class DoctorDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    // ─── Intent Key Constants ────────────────────────────────────────────────
    public static final String EXTRA_DOCTOR_NAME       = "doctor_name";
    public static final String EXTRA_DOCTOR_SPECIALTY  = "doctor_specialty";
    public static final String EXTRA_DOCTOR_RATING     = "doctor_rating";
    public static final String EXTRA_DOCTOR_LAT        = "doctor_lat";
    public static final String EXTRA_DOCTOR_LNG        = "doctor_lng";
    public static final String EXTRA_DOCTOR_ID         = "doctor_id";

    private static final String PREFS_RATINGS = "doctor_ratings";

    // ─── Views ───────────────────────────────────────────────────────────────
    private TextView    tvDoctorName, tvDoctorSpecialty;
    private RatingBar   ratingBar;
    private Calendarview calendarView; // custom view – see Calendarview.java

    // ─── State ───────────────────────────────────────────────────────────────
    private String doctorId;
    private double doctorLat, doctorLng;
    private GoogleMap googleMap;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_details);

        bindViews();
        loadIntentData();
        setupBackButton();
        setupMap();
        setupRatingBar();
        setupCalendar();
        setupBookButton();
    }

    // ── 1. Bind all view references ──────────────────────────────────────────
    private void bindViews() {
        tvDoctorName      = findViewById(R.id.tvDoctorName);
        tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty);
        ratingBar         = findViewById(R.id.doctorRating);
        calendarView      = findViewById(R.id.calendarView);
    }

    // ── 2. Read Intent extras and populate UI ────────────────────────────────
    private void loadIntentData() {
        Intent intent = getIntent();

        doctorId     = intent.getStringExtra(EXTRA_DOCTOR_ID);
        doctorLat    = intent.getDoubleExtra(EXTRA_DOCTOR_LAT, 0.0);
        doctorLng    = intent.getDoubleExtra(EXTRA_DOCTOR_LNG, 0.0);

        String name      = intent.getStringExtra(EXTRA_DOCTOR_NAME);
        String specialty = intent.getStringExtra(EXTRA_DOCTOR_SPECIALTY);
        float  rating    = intent.getFloatExtra(EXTRA_DOCTOR_RATING, 0f);

        if (name      != null) tvDoctorName.setText(name);
        if (specialty != null) tvDoctorSpecialty.setText(specialty);

        // Restore any previously saved rating for this doctor
        if (doctorId != null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_RATINGS, MODE_PRIVATE);
            float savedRating = prefs.getFloat(doctorId, rating);
            ratingBar.setRating(savedRating);
        } else {
            ratingBar.setRating(rating);
        }
    }

    // ── 3. Back button ───────────────────────────────────────────────────────
    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    // ── 4. Google Maps ───────────────────────────────────────────────────────
    private void setupMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        LatLng clinicLocation = new LatLng(doctorLat, doctorLng);

        googleMap.addMarker(new MarkerOptions()
                .position(clinicLocation)
                .title("Clinic"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(clinicLocation, 15f));

        // Disable scroll gestures so the map doesn't steal the ScrollView touch
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Tapping the map opens it full-screen in the Maps app
        CardView cardMap = findViewById(R.id.cardMap);
        cardMap.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse(
                    "geo:" + doctorLat + "," + doctorLng + "?q=" + doctorLat + "," + doctorLng);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });
    }

    // ── 5. RatingBar ─────────────────────────────────────────────────────────
    private void setupRatingBar() {
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (!fromUser || doctorId == null) return;

            // Persist
            SharedPreferences prefs = getSharedPreferences(PREFS_RATINGS, MODE_PRIVATE);
            prefs.edit().putFloat(doctorId, rating).apply();

            // Feedback
            String[] labels = {"", "Poor", "Fair", "Good", "Very good", "Excellent"};
            int idx = Math.min((int) rating, 5);
            Toast.makeText(this,
                    "You rated " + labels[idx] + " (" + (int) rating + "/5)",
                    Toast.LENGTH_SHORT).show();

            // TODO: send to backend
            // ApiClient.submitRating(doctorId, rating);
        });
    }

    // ── 6. Calendar ──────────────────────────────────────────────────────────
    private void setupCalendar() {
        // Calendarview notifies us when the user picks a day
        calendarView.setOnDaySelectedListener(selectedDate -> {
            // Pass the chosen date to BookingActivity
            Button btnBook = findViewById(R.id.btnBookAppointment);
            btnBook.setTag(selectedDate); // store for use in setupBookButton
        });
    }

    // ── 7. Book button ───────────────────────────────────────────────────────
    private void setupBookButton() {
        Button btnBook = findViewById(R.id.btnBookAppointment);
        btnBook.setOnClickListener(v -> {
            Object tag = btnBook.getTag();
            if (tag == null) {
                Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = getIntent(); // re-use for doctor info
            Intent bookingIntent = new Intent(this, BookingActivity.class);

            // Forward all doctor extras
            bookingIntent.putExtra(EXTRA_DOCTOR_ID,        intent.getStringExtra(EXTRA_DOCTOR_ID));
            bookingIntent.putExtra(EXTRA_DOCTOR_NAME,      intent.getStringExtra(EXTRA_DOCTOR_NAME));
            bookingIntent.putExtra(EXTRA_DOCTOR_SPECIALTY, intent.getStringExtra(EXTRA_DOCTOR_SPECIALTY));

            // Add selected date (stored as long millis)
            bookingIntent.putExtra(BookingActivity.EXTRA_SELECTED_DATE, (long) tag);

            startActivity(bookingIntent);
        });
    }
}