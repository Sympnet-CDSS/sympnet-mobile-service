package com.sympnet.app.activities.doctor;
import com.sympnet.app.activities.BaseActivity;
import com.sympnet.app.activities.chat.ChatDetailActivity;
import com.sympnet.app.activities.appointment.BookAppointmentActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.sympnet.app.R;
import com.sympnet.app.model.Conversation;
import com.sympnet.app.model.Doctor;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;
import com.sympnet.app.utils.SessionManager;
import com.sympnet.app.views.Calendarview;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorDetailsActivity extends BaseActivity implements OnMapReadyCallback {

    private static final String TAG = "DoctorDetails";

    public static final String EXTRA_DOCTOR_NAME      = "doctor_name";
    public static final String EXTRA_DOCTOR_SPECIALTY = "doctor_specialty";
    public static final String EXTRA_DOCTOR_RATING    = "doctor_rating";
    public static final String EXTRA_DOCTOR_LAT       = "doctor_lat";
    public static final String EXTRA_DOCTOR_LNG       = "doctor_lng";
    public static final String EXTRA_DOCTOR_ID        = "doctor_id";
    public static final String EXTRA_DOCTOR_ADDRESS   = "doctor_address";

    private static final String PREFS_RATINGS         = "doctor_ratings";
    private static final int    LOCATION_PERMISSION_REQUEST = 1001;
    private static final String DISTANCE_MATRIX_KEY   = "GW1LsWPQ2nGMUEWT9KI8QbHgT6S3tmeLqztbVzYX5Yp4X3tkc3mDki7USn36NUwo";

    private TextView  tvDoctorName, tvDoctorSpecialty, tvDistance, tvNoContact;
    private RatingBar ratingBar;
    private Calendarview calendarView;
    private View      btnContact;
    private Doctor    currentDoctor;

    // Tabs
    private TextView tabProfil, tabAvis, tabLocalisation, tabRendezVous;
    private View sectionProfil, sectionAvis, sectionLocalisation, sectionRendezVous;

    private String   doctorId;
    private double   doctorLat, doctorLng;
    private String   doctorAddress;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_details);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        setupTabs();
        loadIntentData();
        setupBackButton();
        setupMap();
        setupRatingBar();
        setupBookButton();
        requestLocationPermission();
    }

    private void bindViews() {
        tvDoctorName      = findViewById(R.id.tvDoctorName);
        tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty);
        tvDistance        = findViewById(R.id.tvDistance);
        ratingBar         = findViewById(R.id.doctorRating);
        calendarView      = findViewById(R.id.calendarView);
        btnContact        = findViewById(R.id.btnContact);
        tvNoContact       = findViewById(R.id.tvNoContact);

        tabProfil         = findViewById(R.id.tabProfil);
        tabAvis           = findViewById(R.id.tabAvis);
        tabLocalisation   = findViewById(R.id.tabLocalisation);
        tabRendezVous     = findViewById(R.id.tabRendezVous);

        sectionProfil       = findViewById(R.id.sectionProfil);
        sectionAvis         = findViewById(R.id.sectionAvis);
        sectionLocalisation = findViewById(R.id.sectionLocalisation);
        sectionRendezVous   = findViewById(R.id.sectionRendezVous);
    }

    private void setupTabs() {
        tabProfil.setOnClickListener(v -> selectTab(0));
        tabAvis.setOnClickListener(v -> selectTab(1));
        tabLocalisation.setOnClickListener(v -> selectTab(2));
        tabRendezVous.setOnClickListener(v -> selectTab(3));
        
        // Set default active tab
        selectTab(0);
    }

    private void selectTab(int index) {
        // Reset all tabs
        TextView[] tabs = {tabProfil, tabAvis, tabLocalisation, tabRendezVous};
        for (TextView tab : tabs) {
            tab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFFFFF")));
            tab.setTextColor(android.graphics.Color.parseColor("#666666"));
            tab.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        // Set active tab
        tabs[index].setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#009688")));
        tabs[index].setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
        tabs[index].setTypeface(null, android.graphics.Typeface.BOLD);

        // Reset sections
        sectionProfil.setVisibility(View.GONE);
        sectionAvis.setVisibility(View.GONE);
        sectionLocalisation.setVisibility(View.GONE);
        sectionRendezVous.setVisibility(View.GONE);

        // Show active section
        if (index == 0) sectionProfil.setVisibility(View.VISIBLE);
        else if (index == 1) sectionAvis.setVisibility(View.VISIBLE);
        else if (index == 2) sectionLocalisation.setVisibility(View.VISIBLE);
        else if (index == 3) sectionRendezVous.setVisibility(View.VISIBLE);
    }

    private void loadIntentData() {
        Intent intent = getIntent();

        doctorId      = intent.getStringExtra(EXTRA_DOCTOR_ID);
        doctorLat     = intent.getDoubleExtra(EXTRA_DOCTOR_LAT, 0.0);
        doctorLng     = intent.getDoubleExtra(EXTRA_DOCTOR_LNG, 0.0);
        doctorAddress = intent.getStringExtra(EXTRA_DOCTOR_ADDRESS);

        String name      = intent.getStringExtra(EXTRA_DOCTOR_NAME);
        String specialty = intent.getStringExtra(EXTRA_DOCTOR_SPECIALTY);
        float  rating    = intent.getFloatExtra(EXTRA_DOCTOR_RATING, 0f);

        if (name      != null) tvDoctorName.setText(name);
        if (specialty != null) tvDoctorSpecialty.setText(specialty);

        if (doctorId != null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_RATINGS, MODE_PRIVATE);
            ratingBar.setRating(prefs.getFloat(doctorId, rating));
        } else {
            ratingBar.setRating(rating);
        }

        currentDoctor = new Doctor();
        currentDoctor.setId(doctorId != null ? Integer.parseInt(doctorId) : -1);
        if (name != null) {
            String[] parts = name.split(" ", 2);
            currentDoctor.setFirstName(parts.length > 0 ? parts[0] : "");
            currentDoctor.setLastName(parts.length > 1  ? parts[1] : "");
        }
        currentDoctor.setSpeciality(specialty);
        currentDoctor.setAddress(doctorAddress);

        if (doctorId != null) checkIfCanContact(doctorId);
    }

    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (doctorLat == 0.0 && doctorLng == 0.0 && doctorAddress != null) {
            geocodeAddress(doctorAddress);
        } else {
            placeMarkerAndZoom(doctorLat, doctorLng,
                    doctorAddress != null ? doctorAddress : "Cabinet médical");
        }
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        CardView cardMap = findViewById(R.id.cardMap);
        cardMap.setOnClickListener(v -> openInMapsApp(doctorLat, doctorLng));
    }

    private void geocodeAddress(String address) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(address, 1);
                if (results != null && !results.isEmpty()) {
                    Address result = results.get(0);
                    doctorLat = result.getLatitude();
                    doctorLng = result.getLongitude();
                    runOnUiThread(() -> placeMarkerAndZoom(doctorLat, doctorLng, address));
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding error", e);
            }
        }).start();
    }

    private void placeMarkerAndZoom(double lat, double lng, String title) {
        LatLng clinicLocation = new LatLng(lat, lng);
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(clinicLocation).title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(clinicLocation, 15f));
        if (userLocation != null) showDistanceAndLine(clinicLocation);
        else requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getUserLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) onUserLocationObtained(location);
            else requestFreshLocation();
        });
    }

    @SuppressWarnings("MissingPermission")
    private void requestFreshLocation() {
        LocationRequest req = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1).setInterval(5000).setFastestInterval(2000);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult r) {
                if (r == null || r.getLocations().isEmpty()) return;
                fusedLocationClient.removeLocationUpdates(locationCallback);
                onUserLocationObtained(r.getLocations().get(0));
            }
        };
        fusedLocationClient.requestLocationUpdates(req, locationCallback, null);
    }

    private void onUserLocationObtained(Location location) {
        userLocation = location;
        if (googleMap != null) {
            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(userLatLng).title("Vous êtes ici")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            if (doctorLat != 0.0 && doctorLng != 0.0)
                showDistanceAndLine(new LatLng(doctorLat, doctorLng));
        }
    }

    private void showDistanceAndLine(LatLng clinicLatLng) {
        if (userLocation == null) return;
        LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        googleMap.addPolyline(new PolylineOptions()
                .add(userLatLng, clinicLatLng).width(4f).color(0xFF009688));

        String origins      = userLocation.getLatitude() + "," + userLocation.getLongitude();
        String destinations = clinicLatLng.latitude + "," + clinicLatLng.longitude;
        String url = "https://api.distancematrix.ai/maps/api/distancematrix/json"
                + "?origins=" + origins + "&destinations=" + destinations
                + "&mode=driving&key=" + DISTANCE_MATRIX_KEY;

        new Thread(() -> {
            try {
                java.net.URL requestUrl = new java.net.URL(url);
                java.net.HttpURLConnection conn =
                        (java.net.HttpURLConnection) requestUrl.openConnection();
                conn.setRequestMethod("GET");
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                org.json.JSONObject json = new org.json.JSONObject(sb.toString());
                if (json.optString("status").equals("OK")) {
                    org.json.JSONObject el = json.getJSONArray("rows")
                            .getJSONObject(0).getJSONArray("elements").getJSONObject(0);
                    if (el.getString("status").equals("OK")) {
                        String dist = el.getJSONObject("distance").getString("text");
                        String dur  = el.getJSONObject("duration").getString("text");
                        runOnUiThread(() -> {
                            if (tvDistance != null)
                                tvDistance.setText("🚗 " + dist + " · ⏱ " + dur);
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "distancematrix failed", e);
            }
        }).start();
    }

    private void openInMapsApp(double lat, double lng) {
        Uri uri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
    }

    private void setupRatingBar() {
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (!fromUser || doctorId == null) return;
            getSharedPreferences(PREFS_RATINGS, MODE_PRIVATE)
                    .edit().putFloat(doctorId, rating).apply();
        });
    }

    private void setupBookButton() {
        findViewById(R.id.btnBookAppointment).setOnClickListener(v -> {
            Intent intent = new Intent(this, BookAppointmentActivity.class);
            intent.putExtra("doctorId", doctorId != null ? Integer.parseInt(doctorId) : -1);
            intent.putExtra("doctorName", tvDoctorName.getText().toString());
            intent.putExtra("doctorSpecialty", tvDoctorSpecialty.getText().toString());
            intent.putExtra("doctorAddress", doctorAddress != null ? doctorAddress : "Cabinet médical");
            startActivity(intent);
        });
    }

    //  Contact 

    private void checkIfCanContact(String doctorId) {
        String patientId = SessionManager.getInstance(this).getCurrentUserId();
        String token     = "Bearer " + SessionManager.getInstance(this).getUserToken();

        ApiClient.getClient().create(ApiService.class)
                .getConfirmedAppointments(token, patientId, doctorId)
                .enqueue(new Callback<List<Object>>() {
                    @Override
                    public void onResponse(Call<List<Object>> call,
                                           Response<List<Object>> response) {
                        boolean hasConfirmed = response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty();

                        if (btnContact != null)
                            btnContact.setVisibility(hasConfirmed ? View.VISIBLE : View.GONE);
                        
                        if (tvNoContact != null) {
                            tvNoContact.setVisibility(hasConfirmed ? View.GONE : View.VISIBLE);
                            if (!hasConfirmed)
                                tvNoContact.setText(
                                        "Prenez un rendez-vous pour contacter ce médecin");
                        }

                        if (hasConfirmed) {
                            if (btnContact != null)
                                btnContact.setOnClickListener(v -> openChat(currentDoctor));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Object>> call, Throwable t) {
                        Log.e(TAG, "checkIfCanContact failed", t);
                    }
                });
    }

    private void openChat(Doctor doctor) {
        String token     = "Bearer " + SessionManager.getInstance(this).getUserToken();
        String patientId = SessionManager.getInstance(this).getCurrentUserId();

        Map<String, String> body = new HashMap<>();
        body.put("doctorId",  String.valueOf(doctor.getId()));
        body.put("patientId", patientId);

        ApiClient.getClient().create(ApiService.class)
                .createConversation(token, body)
                .enqueue(new Callback<Conversation>() {
                    @Override
                    public void onResponse(Call<Conversation> call,
                                           Response<Conversation> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Conversation conv = response.body();
                            conv.setOtherUserName(doctor.getFullName());
                            conv.setOtherUserRole(doctor.getSpecialty());
                            ChatDetailActivity.start(DoctorDetailsActivity.this, conv);
                        } else {
                            Toast.makeText(DoctorDetailsActivity.this,
                                    "Erreur " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Conversation> call, Throwable t) {
                        Toast.makeText(DoctorDetailsActivity.this,
                                "Erreur connexion", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}