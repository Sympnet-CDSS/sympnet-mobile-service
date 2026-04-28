package com.sympnet.app.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.sympnet.app.R;
import com.sympnet.app.activities.LoginActivity;
import com.sympnet.app.activities.MainActivity;
import com.sympnet.app.activities.NotificationDetailsActivity;
import com.sympnet.app.activities.SettingsActivity;
import com.sympnet.app.adapters.DoctorAdapter;
import com.sympnet.app.model.Doctor;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityHome extends AppCompatActivity {

    private static final String TAG = "ActivityHome";

    private RecyclerView recyclerDoctors;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> allDoctors = new ArrayList<>();

    private ImageView btnNotifications, btnSettings, ivAvatar; // ← ivAvatar ajouté
    private TextView tvPatientName, tvCurrentDate;
    private EditText etSearch;
    private ImageView navHome, navChat, navProfile, navCalendar;

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

        initViews();
        loadUserData(prefs);   // ← charge nom + photo
        setupRecyclerView();
        setupBottomNav();
        setupSearch();
        loadDoctors();
        updateNavIcons(navHome);
    }

    // ── onResume : rafraîchit la photo si elle change dans EditProfile ────────
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        loadUserData(prefs);
    }

    private void initViews() {
        tvPatientName    = findViewById(R.id.tvPatientName);
        tvCurrentDate    = findViewById(R.id.tvCurrentDate);
        etSearch         = findViewById(R.id.etSearch);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnSettings      = findViewById(R.id.btnSettings);
        recyclerDoctors  = findViewById(R.id.recyclerDoctors);
        ivAvatar         = findViewById(R.id.ivAvatar);          // ← ajouté
        navHome          = findViewById(R.id.nav_home_icon);
        navChat          = findViewById(R.id.nav_chat_icon);
        navProfile       = findViewById(R.id.nav_profile_icon);
        navCalendar      = findViewById(R.id.nav_calendar_icon);
    }

    private void loadUserData(SharedPreferences prefs) {
        // ── Nom ──────────────────────────────────────────────────────────────
        String name = prefs.getString("userName", "").trim();
        tvPatientName.setText(name.isEmpty() ? "Patient" : name);

        // ── Date ─────────────────────────────────────────────────────────────
        String date = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(new Date());
        tvCurrentDate.setText(date);

        // ── Photo de profil ───────────────────────────────────────────────────
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

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            updateNavIcons(navHome);
            loadDoctors();
        });

        navChat.setOnClickListener(v -> {
            updateNavIcons(navChat);
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("TARGET_FRAGMENT", "CHAT"));
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
        navProfile.setColorFilter(inactive);
        navCalendar.setColorFilter(inactive);
        selected.setColorFilter(active);
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
            if (d.getName() != null && d.getName().toLowerCase().contains(lower)) {
                filtered.add(d);
            }
        }
        doctorAdapter.updateList(filtered);
    }
}