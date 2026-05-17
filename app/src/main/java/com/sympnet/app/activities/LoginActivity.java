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

    private static final String TAG        = "LoginActivity";
    private static final String PREFS_NAME = "SympNetPrefs";

    private TextInputEditText etEmail, etPassword;
    private Button            btnLogin;
    private TextView          tvForgotPassword, tvRegister;
    private ProgressBar       progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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

    private void performLogin(String email, String password) {
        setLoading(true);
        ApiService api = ApiClient.getClient().create(ApiService.class);
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        api.login(body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    saveAuthData(user, email);
                    fetchPatientProfile(user);
                } else {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchPatientProfile(User user) {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        String userId      = user.getUserId();
        String bearerToken = "Bearer " + user.getToken();

        api.getPatientByUserId(bearerToken, userId).enqueue(new Callback<Patient>() {
            @Override
            public void onResponse(Call<Patient> call, Response<Patient> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Patient patient = response.body();
                    mergeAndSavePatientData(patient);
                    String displayName = patient.getFullName();
                    if (displayName.trim().isEmpty()) displayName = user.getEmail();
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString("userName", displayName).apply();
                }
                goHome();
            }

            @Override
            public void onFailure(Call<Patient> call, Throwable t) {
                setLoading(false);
                goHome();
            }
        });
    }

    private void saveAuthData(User user, String emailFallback) {
        String email       = !user.getEmail().isEmpty() ? user.getEmail() : emailFallback;
        String initialName = !user.getFullName().isEmpty() ? user.getFullName() : email;

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putBoolean("isLoggedIn", true)
                .putString("userId",      user.getUserId())
                .putString("userToken",   user.getToken())
                .putString("userEmail",   email)
                .putString("userRole",    user.getRole())
                .putString("userName",    initialName)
                .apply();
    }

    private void mergeAndSavePatientData(Patient patient) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString("patientId", String.valueOf(patient.getId()))
                .putString("userFirstName", patient.getFirstName())
                .putString("userLastName", patient.getLastName())
                .putString("userPhone", patient.getPhoneNumber())
                .apply();
    }

    private void goHome() {
        startActivity(new Intent(this, ActivityHome.class));
        finish();
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }

    private String text(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }
}
