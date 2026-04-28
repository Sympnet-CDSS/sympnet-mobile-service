package com.sympnet.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.sympnet.app.R;
import com.sympnet.app.home.ActivityHome;
import com.sympnet.app.model.Patient;
import com.sympnet.app.model.User;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Skip login if session already exists
        SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isLoggedIn", false)) {
            goHome();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister       = findViewById(R.id.tvRegister);
        progressBar      = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> {
            String email    = text(etEmail);
            String password = text(etPassword);

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            performLogin(email, password);
        });

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    // ── Step 1: authenticate ──────────────────────────────────────────────

    private void performLogin(String email, String password) {
        setLoading(true);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Log.d(TAG, "Tentative login avec email: " + email);

        api.login(body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, "HTTP code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d(TAG, "Token: " + user.getToken());
                    Log.d(TAG, "Email: " + user.getEmail());
                    Log.d(TAG, "UserId: " + user.getUserId());
                    Log.d(TAG, "Role: " + user.getRole());
                    Log.d(TAG, "FullName: " + user.getFullName());

                    saveAuthData(user, email);
                    fetchPatientProfile(user);
                } else {
                    try {
                        String errorBody = response.errorBody() != null
                                ? response.errorBody().string()
                                : "null";
                        Log.e(TAG, "Erreur login - HTTP " + response.code() + " body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Impossible de lire le error body");
                    }
                    setLoading(false);
                    Toast.makeText(LoginActivity.this,
                            "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Login failed - erreur réseau: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this,
                        "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── Step 2: fetch Patient profile using UserId from login response ────

    private void fetchPatientProfile(User user) {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        String bearerToken = "Bearer " + user.getToken();
        String userId      = user.getUserId();

        Log.d(TAG, "fetchPatientProfile pour userId: " + userId);

        api.getPatientByUserId(bearerToken, userId).enqueue(new Callback<Patient>() {
            @Override
            public void onResponse(Call<Patient> call, Response<Patient> response) {
                setLoading(false);
                Log.d(TAG, "getPatientByUserId HTTP code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Patient patient = response.body();
                    savePatientData(patient);

                    String displayName = patient.getFullName();
                    if (displayName.isEmpty()) displayName = user.getEmail();

                    getSharedPreferences("SympNetPrefs", MODE_PRIVATE)
                            .edit()
                            .putString("userName", displayName)
                            .apply();

                    Toast.makeText(LoginActivity.this,
                            "Welcome " + displayName, Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "No patient profile found for userId=" + userId
                            + " HTTP " + response.code());
                    Toast.makeText(LoginActivity.this,
                            "Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();
                }
                goHome();
            }

            @Override
            public void onFailure(Call<Patient> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "fetchPatientProfile failed: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this,
                        "Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();
                goHome();
            }
        });
    }

    // ── SharedPreferences helpers ─────────────────────────────────────────

    private void saveAuthData(User user, String emailFallback) {
        String email = !user.getEmail().isEmpty() ? user.getEmail() : emailFallback;

        String initialName = !user.getFullName().isEmpty()
                ? user.getFullName()
                : email;

        getSharedPreferences("SympNetPrefs", MODE_PRIVATE).edit()
                .putBoolean("isLoggedIn",  true)
                .putString("userId",       user.getUserId())
                .putString("userToken",    user.getToken())
                .putString("userEmail",    email)
                .putString("userRole",     user.getRole())
                .putString("userName",     initialName)
                .apply();
    }

    private void savePatientData(Patient patient) {
        getSharedPreferences("SympNetPrefs", MODE_PRIVATE).edit()
                .putString("patientId",          String.valueOf(patient.getId()))
                .putString("userFirstName",       patient.getFirstName())
                .putString("userLastName",        patient.getLastName())
                .putString("userPhone",           patient.getPhoneNumber())
                .putString("userDob",             patient.getDateOfBirth())
                .putString("userGender",          patient.getGender())
                .putString("userAddress",         patient.getAddress())
                .putString("userBloodType",       patient.getBloodType())
                .putString("userAllergies",       patient.getAllergies())
                .putString("userMedicalHistory",  patient.getMedicalHistory())
                .putInt   ("consultationCount",   patient.getConsultationCount())
                .apply();
    }

    // ── Utilities ─────────────────────────────────────────────────────────

    private void goHome() {
        startActivity(new Intent(this, ActivityHome.class));
        finish();
    }

    private void setLoading(boolean loading) {
        if (progressBar != null)
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Signing in…" : "Sign In");
    }

    private String text(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }
}