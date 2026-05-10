package com.sympnet.app.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.sympnet.app.R;
import com.sympnet.app.activities.AllDoctorsActivity;
import com.sympnet.app.activities.LoginActivity;
import com.sympnet.app.activities.MainActivity;
import com.sympnet.app.activities.NotificationDetailsActivity;
import com.sympnet.app.activities.SettingsActivity;
import com.sympnet.app.adapters.DoctorAdapter;
import com.sympnet.app.model.Doctor;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityHome extends AppCompatActivity {

    private static final String TAG = "ActivityHome";
    private static final int LOCATION_PERMISSION_REQUEST = 2001;

    private RecyclerView recyclerDoctors;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> allDoctors = new ArrayList<>();

    private ImageView btnNotifications, btnSettings, ivAvatar;
    private TextView tvPatientName, tvCurrentDate, tvVoirTout;
    private EditText etSearch;
    private ImageView navHome, navChat, navAi, navProfile, navCalendar;

    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        loadUserData(prefs);
        setupRecyclerView();
        setupBottomNav();
        setupSearch();
        setupVoirTout();
        requestLocationAndLoadDoctors();
        updateNavIcons(navHome);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        loadUserData(prefs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void initViews() {
        tvPatientName    = findViewById(R.id.tvPatientName);
        tvCurrentDate    = findViewById(R.id.tvCurrentDate);
        tvVoirTout       = findViewById(R.id.tvVoirTout);
        etSearch         = findViewById(R.id.etSearch);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnSettings      = findViewById(R.id.btnSettings);
        recyclerDoctors  = findViewById(R.id.recyclerDoctors);
        ivAvatar         = findViewById(R.id.ivAvatar);
        navHome          = findViewById(R.id.nav_home_icon);
        navChat          = findViewById(R.id.nav_chat_icon);
        navAi            = findViewById(R.id.nav_ai_icon);
        navProfile       = findViewById(R.id.nav_profile_icon);
        navCalendar      = findViewById(R.id.nav_calendar_icon);
    }

    private void loadUserData(SharedPreferences prefs) {
        String name = prefs.getString("userName", "").trim();
        tvPatientName.setText(name.isEmpty() ? "Patient" : name);

        String date = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(new Date());
        tvCurrentDate.setText(date);

        if (ivAvatar != null) {
            String base64 = prefs.getString("userPhotoBase64", null);
            if (base64 != null) {
                try {
                    byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bmp   = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bmp != null) {
                        Glide.with(this)
                                .load(bmp)
                                .transform(new CircleCrop())
                                .placeholder(R.drawable.ic_profile_avatar)
                                .into(ivAvatar);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading photo", e);
                    loadDefaultAvatar();
                }
            } else {
                loadDefaultAvatar();
            }
        }
    }

    private void loadDefaultAvatar() {
        if (ivAvatar != null) {
            Glide.with(this)
                    .load(R.drawable.ic_profile_avatar)
                    .transform(new CircleCrop())
                    .into(ivAvatar);
        }
    }

    private void setupRecyclerView() {
        recyclerDoctors.setLayoutManager(new LinearLayoutManager(this));
        recyclerDoctors.setHasFixedSize(false);
    }

    private void setupVoirTout() {
        if (tvVoirTout != null) {
            tvVoirTout.setOnClickListener(v ->
                    startActivity(new Intent(this, AllDoctorsActivity.class)));
        }
    }

    private void requestLocationAndLoadDoctors() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserLocationThenLoadDoctors();
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
            getUserLocationThenLoadDoctors();
        } else {
            Log.w(TAG, "Location permission denied — loading without distance sort");
            loadDoctors();
        }
    }

    @SuppressLint("MissingPermission")
    private void getUserLocationThenLoadDoctors() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Log.d(TAG, "Location OK: " + location.getLatitude()
                        + ", " + location.getLongitude());
                userLocation = location;
                loadDoctors();
            } else {
                Log.w(TAG, "getLastLocation null — requesting fresh location");
                requestFreshLocation();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "getLastLocation failed", e);
            loadDoctors();
        });
    }

    @SuppressLint("MissingPermission")
    private void requestFreshLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
                .setInterval(5000)
                .setFastestInterval(2000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    userLocation = locationResult.getLocations().get(0);
                    Log.d(TAG, "Fresh location OK: " + userLocation.getLatitude()
                            + ", " + userLocation.getLongitude());
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
                loadDoctors();
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void loadDoctors() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getDoctors().enqueue(new Callback<List<Doctor>>() {
            @Override
            public void onResponse(Call<List<Doctor>> call, Response<List<Doctor>> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && !response.body().isEmpty()) {

                    allDoctors = response.body();

                    if (userLocation != null) {
                        sortDoctorsByDistance(allDoctors);
                        Log.d(TAG, "Doctors sorted by distance from user");
                    } else {
                        Log.d(TAG, "No location — doctors loaded without sorting");
                    }

                    // ✅ Sauvegarde le cache pour FavoriteDoctorsActivity
                    cacheDoctors(allDoctors);

                    doctorAdapter = new DoctorAdapter(allDoctors);
                    recyclerDoctors.setAdapter(doctorAdapter);
                    Log.d(TAG, "Loaded " + allDoctors.size() + " doctors");

                } else {
                    Log.w(TAG, "Empty doctor list. HTTP " + response.code());
                    Toast.makeText(ActivityHome.this,
                            "No doctors available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Doctor>> call, Throwable t) {
                Log.e(TAG, "loadDoctors failed", t);
                Toast.makeText(ActivityHome.this,
                        "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sortDoctorsByDistance(List<Doctor> doctors) {
        Collections.sort(doctors, (d1, d2) -> {
            double dist1 = calculateHaversineDistance(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    d1.getLatitude(), d1.getLongitude());
            double dist2 = calculateHaversineDistance(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    d2.getLatitude(), d2.getLongitude());
            return Double.compare(dist1, dist2);
        });
    }

    private double calculateHaversineDistance(double lat1, double lon1,
                                              double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ✅ Sauvegarde tous les médecins en cache JSON pour les favoris
    private void cacheDoctors(List<Doctor> doctors) {
        try {
            JSONArray array = new JSONArray();
            for (Doctor d : doctors) {
                JSONObject obj = new JSONObject();
                obj.put("id",         d.getId());
                obj.put("firstName",  d.getFirstName());
                obj.put("lastName",   d.getLastName());
                obj.put("speciality", d.getSpecialty());
                obj.put("rating",     d.getRating());
                obj.put("latitude",   d.getLatitude());
                obj.put("longitude",  d.getLongitude());
                obj.put("address",    d.getAddress());
                array.put(obj);
            }
            getSharedPreferences("doctors_cache", MODE_PRIVATE)
                    .edit()
                    .putString("all_doctors", array.toString())
                    .apply();
            Log.d(TAG, "Doctors cached: " + doctors.size());
        } catch (Exception e) {
            Log.e(TAG, "cacheDoctors failed", e);
        }
    }

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            updateNavIcons(navHome);
            requestLocationAndLoadDoctors();
        });

        navChat.setOnClickListener(v -> {
            updateNavIcons(navChat);
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("TARGET_FRAGMENT", "CHAT"));
        });

        navAi.setOnClickListener(v -> {
            updateNavIcons(navAi);
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("TARGET_FRAGMENT", "CHATBOT"));
        });

        navCalendar.setOnClickListener(v -> {
            updateNavIcons(navCalendar);
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("TARGET_FRAGMENT", "SCHEDULE"));
        });

        navProfile.setOnClickListener(v -> {
            updateNavIcons(navProfile);
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("TARGET_FRAGMENT", "PROFILE"));
        });

        btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationDetailsActivity.class)));

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void updateNavIcons(ImageView selected) {
        int active   = ContextCompat.getColor(this, R.color.white);
        int inactive = Color.parseColor("#B2DFDB");
        navHome.setColorFilter(inactive);
        navChat.setColorFilter(inactive);
        navAi.setColorFilter(inactive);
        navProfile.setColorFilter(inactive);
        navCalendar.setColorFilter(inactive);
        selected.setColorFilter(active);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors(s.toString());
            }
        });
    }

    private void filterDoctors(String query) {
        if (doctorAdapter == null) return;

        if (query.trim().isEmpty()) {
            doctorAdapter.updateList(allDoctors);
            return;
        }

        String lower = query.toLowerCase().trim();
        List<Doctor> filtered = new ArrayList<>();
        for (Doctor d : allDoctors) {
            boolean matchName      = d.getName() != null
                    && d.getName().toLowerCase().contains(lower);
            boolean matchSpecialty = d.getSpecialty() != null
                    && d.getSpecialty().toLowerCase().contains(lower);
            if (matchName || matchSpecialty) {
                filtered.add(d);
            }
        }
        doctorAdapter.updateList(filtered);
    }
}