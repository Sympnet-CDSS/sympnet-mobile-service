package com.sympnet.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.sympnet.app.views.Calendarview;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DoctorDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "DoctorDetails";

    public static final String EXTRA_DOCTOR_NAME      = "doctor_name";
    public static final String EXTRA_DOCTOR_SPECIALTY = "doctor_specialty";
    public static final String EXTRA_DOCTOR_RATING    = "doctor_rating";
    public static final String EXTRA_DOCTOR_LAT       = "doctor_lat";
    public static final String EXTRA_DOCTOR_LNG       = "doctor_lng";
    public static final String EXTRA_DOCTOR_ID        = "doctor_id";
    public static final String EXTRA_DOCTOR_ADDRESS   = "doctor_address";

    private static final String PREFS_RATINGS = "doctor_ratings";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String DISTANCE_MATRIX_KEY = "GW1LsWPQ2nGMUEWT9KI8QbHgT6S3tmeLqztbVzYX5Yp4X3tkc3mDki7USn36NUwo";

    private TextView tvDoctorName, tvDoctorSpecialty, tvDistance;
    private RatingBar ratingBar;
    private Calendarview calendarView;

    private String doctorId;
    private double doctorLat, doctorLng;
    private String doctorAddress;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;

    // ── Utilisé pour le fallback si getLastLocation() retourne null ──────────
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_details);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        loadIntentData();
        setupBackButton();
        setupMap();
        setupRatingBar();
        setupCalendar();
        setupBookButton();
        requestLocationPermission();
    }

    private void bindViews() {
        tvDoctorName      = findViewById(R.id.tvDoctorName);
        tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty);
        tvDistance        = findViewById(R.id.tvDistance);
        ratingBar         = findViewById(R.id.doctorRating);
        calendarView      = findViewById(R.id.calendarView);
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

        Log.d(TAG, "Doctor: " + name + " | lat=" + doctorLat + " lng=" + doctorLng
                + " | address=" + doctorAddress);

        if (name      != null) tvDoctorName.setText(name);
        if (specialty != null) tvDoctorSpecialty.setText(specialty);

        if (doctorId != null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_RATINGS, MODE_PRIVATE);
            ratingBar.setRating(prefs.getFloat(doctorId, rating));
        } else {
            ratingBar.setRating(rating);
        }
    }

    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

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

        // Si lat/lng sont 0, on géocode l'adresse
        if (doctorLat == 0.0 && doctorLng == 0.0 && doctorAddress != null) {
            Log.d(TAG, "Coordinates missing — geocoding address: " + doctorAddress);
            geocodeAddress(doctorAddress);
        } else {
            Log.d(TAG, "Using coordinates directly: " + doctorLat + ", " + doctorLng);
            placeMarkerAndZoom(doctorLat, doctorLng,
                    doctorAddress != null ? doctorAddress : "Cabinet médical");
        }

        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        CardView cardMap = findViewById(R.id.cardMap);
        cardMap.setOnClickListener(v -> openInMapsApp(doctorLat, doctorLng));
    }

    // ── Géocodage adresse → coordonnées ──────────────────────────────────────
    private void geocodeAddress(String address) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(address, 1);
                if (results != null && !results.isEmpty()) {
                    Address result = results.get(0);
                    doctorLat = result.getLatitude();
                    doctorLng = result.getLongitude();
                    Log.d(TAG, "Geocoded → lat=" + doctorLat + " lng=" + doctorLng);
                    runOnUiThread(() ->
                            placeMarkerAndZoom(doctorLat, doctorLng, address));
                } else {
                    Log.w(TAG, "Geocoding returned no results for: " + address);
                    runOnUiThread(() ->
                            Toast.makeText(this, "Adresse introuvable", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding error", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Erreur géocodage", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // ── Place le marker et zoome ──────────────────────────────────────────────
    // CORRECTIF : on recalcule la distance ici si on a déjà userLocation
    private void placeMarkerAndZoom(double lat, double lng, String title) {
        LatLng clinicLocation = new LatLng(lat, lng);

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(clinicLocation)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(clinicLocation, 15f));

        // Si on a déjà la position GPS (ex: géocodage arrive après getUserLocation)
        if (userLocation != null) {
            Log.d(TAG, "userLocation already available — computing distance immediately");
            showDistanceAndLine(clinicLocation);
        } else {
            // Sinon on relance la récupération GPS (cas géocodage)
            Log.d(TAG, "userLocation null — requesting location again");
            requestLocationPermission();
        }
    }

    // ── Demande la permission de localisation ────────────────────────────────
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            Log.w(TAG, "Location permission denied");
            if (tvDistance != null)
                tvDistance.setText("Permission GPS refusée");
        }
    }

    // ── Récupère la position GPS — avec fallback LocationRequest ─────────────
    @SuppressWarnings("MissingPermission")
    private void getUserLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Log.d(TAG, "getLastLocation OK: " + location.getLatitude()
                        + ", " + location.getLongitude());
                onUserLocationObtained(location);
            } else {
                // getLastLocation() peut retourner null sur émulateur ou GPS éteint
                Log.w(TAG, "getLastLocation returned null — requesting fresh location");
                requestFreshLocation();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "getLastLocation failed", e);
            if (tvDistance != null)
                tvDistance.setText("Position GPS indisponible");
        });
    }

    // ── Fallback : demande une position fraîche si getLastLocation = null ────
    @SuppressWarnings("MissingPermission")
    private void requestFreshLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)           // une seule mise à jour suffit
                .setInterval(5000)
                .setFastestInterval(2000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLocations().isEmpty()) {
                    Log.w(TAG, "Fresh location result empty");
                    return;
                }
                Location loc = locationResult.getLocations().get(0);
                Log.d(TAG, "Fresh location OK: " + loc.getLatitude()
                        + ", " + loc.getLongitude());
                fusedLocationClient.removeLocationUpdates(locationCallback);
                onUserLocationObtained(loc);
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    // ── Appelé une fois qu'on a la position de l'utilisateur ─────────────────
    private void onUserLocationObtained(Location location) {
        userLocation = location;

        if (googleMap != null) {
            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(userLatLng)
                    .title("Vous êtes ici")
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN)));

            // Calcule la distance si les coordonnées du docteur sont disponibles
            if (doctorLat != 0.0 && doctorLng != 0.0) {
                showDistanceAndLine(new LatLng(doctorLat, doctorLng));
            } else {
                Log.w(TAG, "Doctor coordinates not yet available — distance will be computed after geocoding");
            }
        }
    }

    // ── Calcule et affiche la distance + ligne ───────────────────────────────
    private void showDistanceAndLine(LatLng clinicLatLng) {
        if (userLocation == null) {
            Log.w(TAG, "showDistanceAndLine called but userLocation is null");
            return;
        }

        Log.d(TAG, "Computing distance to (" + clinicLatLng.latitude
                + ", " + clinicLatLng.longitude + ")");

        // Ligne droite sur la carte
        LatLng userLatLng = new LatLng(
                userLocation.getLatitude(), userLocation.getLongitude());

        googleMap.addPolyline(new PolylineOptions()
                .add(userLatLng, clinicLatLng)
                .width(4f)
                .color(0xFF009688));

        // ── Appel distancematrix.ai (même format JSON que Google) ────────────
        String origins      = userLocation.getLatitude() + "," + userLocation.getLongitude();
        String destinations = clinicLatLng.latitude + "," + clinicLatLng.longitude;

        // URL distancematrix.ai — remplace juste le domaine vs Google
        String url = "https://api.distancematrix.ai/maps/api/distancematrix/json"
                + "?origins=" + origins
                + "&destinations=" + destinations
                + "&mode=driving"
                + "&key=" + DISTANCE_MATRIX_KEY;

        Log.d(TAG, "distancematrix.ai URL: " + url);

        new Thread(() -> {
            try {
                java.net.URL requestUrl = new java.net.URL(url);
                java.net.HttpURLConnection connection =
                        (java.net.HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "HTTP response code: " + responseCode);

                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String jsonString = response.toString();
                Log.d(TAG, "Raw response: " + jsonString);

                // ── Parse JSON — format identique à Google Distance Matrix ──
                org.json.JSONObject json = new org.json.JSONObject(jsonString);

                String globalStatus = json.optString("status", "UNKNOWN");
                Log.d(TAG, "Global status: " + globalStatus);

                if (!globalStatus.equals("OK")) {
                    String errorMsg = json.optString("error_message", "Erreur inconnue");
                    Log.e(TAG, "API error: " + globalStatus + " | " + errorMsg);
                    runOnUiThread(() -> {
                        if (tvDistance != null)
                            tvDistance.setText("Erreur API: " + globalStatus);
                    });
                    return;
                }

                org.json.JSONObject element = json
                        .getJSONArray("rows").getJSONObject(0)
                        .getJSONArray("elements").getJSONObject(0);

                String elementStatus = element.getString("status");
                Log.d(TAG, "Element status: " + elementStatus);

                if (elementStatus.equals("OK")) {
                    String distanceText = element.getJSONObject("distance").getString("text");
                    String durationText = element.getJSONObject("duration").getString("text");
                    Log.d(TAG, "Distance: " + distanceText + " | Duration: " + durationText);

                    runOnUiThread(() -> {
                        if (tvDistance != null) {
                            tvDistance.setText("🚗 " + distanceText + " · ⏱ " + durationText);
                        }
                    });
                } else {
                    Log.e(TAG, "Element status not OK: " + elementStatus);
                    // Fallback Haversine si distancematrix.ai échoue
                    showHaversineDistance(clinicLatLng);
                }

            } catch (Exception e) {
                Log.e(TAG, "distancematrix.ai call failed: " + e.getMessage(), e);
                // Fallback Haversine en cas d'erreur réseau
                runOnUiThread(() -> showHaversineDistance(clinicLatLng));
            }
        }).start();
    }
    // ── Fallback Haversine si distancematrix.ai échoue ───────────────────────
    private void showHaversineDistance(LatLng clinicLatLng) {
        if (userLocation == null) return;

        double distanceKm = calculateHaversineDistance(
                userLocation.getLatitude(), userLocation.getLongitude(),
                clinicLatLng.latitude, clinicLatLng.longitude);

        int estimatedMinutes = (int) (distanceKm / 40.0 * 60);

        String distanceText = distanceKm < 1.0
                ? (int)(distanceKm * 1000) + " m"
                : String.format(Locale.getDefault(), "%.1f km", distanceKm);

        String durationText = estimatedMinutes < 60
                ? estimatedMinutes + " min"
                : (estimatedMinutes / 60) + "h" + (estimatedMinutes % 60) + "min";

        if (tvDistance != null) {
            tvDistance.setText("📍 " + distanceText + " · ⏱ ~" + durationText);
        }
    }

    // Formule Haversine — calcule la distance entre 2 coordonnées GPS
    private double calculateHaversineDistance(double lat1, double lon1,
                                              double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void openInMapsApp(double lat, double lng) {
        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    private void setupRatingBar() {
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (!fromUser || doctorId == null) return;
            SharedPreferences prefs = getSharedPreferences(PREFS_RATINGS, MODE_PRIVATE);
            prefs.edit().putFloat(doctorId, rating).apply();
            String[] labels = {"", "Poor", "Fair", "Good", "Very good", "Excellent"};
            int idx = Math.min((int) rating, 5);
            Toast.makeText(this,
                    "You rated " + labels[idx] + " (" + (int) rating + "/5)",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void setupCalendar() {
        calendarView.setOnDaySelectedListener(selectedDate -> {
            Button btnBook = findViewById(R.id.btnBookAppointment);
            btnBook.setTag(selectedDate);
        });
    }

    private void setupBookButton() {
        Button btnBook = findViewById(R.id.btnBookAppointment);
        btnBook.setOnClickListener(v -> {
            Object tag = btnBook.getTag();
            if (tag == null) {
                Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = getIntent();
            Intent bookingIntent = new Intent(this, BookingActivity.class);
            bookingIntent.putExtra(EXTRA_DOCTOR_ID,        intent.getStringExtra(EXTRA_DOCTOR_ID));
            bookingIntent.putExtra(EXTRA_DOCTOR_NAME,      intent.getStringExtra(EXTRA_DOCTOR_NAME));
            bookingIntent.putExtra(EXTRA_DOCTOR_SPECIALTY, intent.getStringExtra(EXTRA_DOCTOR_SPECIALTY));
            bookingIntent.putExtra(BookingActivity.EXTRA_SELECTED_DATE, (long) tag);
            startActivity(bookingIntent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Nettoyage du callback de localisation pour éviter les fuites mémoire
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}