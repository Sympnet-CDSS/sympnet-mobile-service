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

    // ── Step 1 : Authentification ─────────────────────────────────────────────

    private void performLogin(String email, String password) {
        setLoading(true);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Log.d(TAG, "Login avec email: " + email);

        api.login(body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, "HTTP: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d(TAG, "Token=" + user.getToken()
                            + " userId=" + user.getUserId()
                            + " name=" + user.getFullName());
                    saveAuthData(user, email);
                    fetchPatientProfile(user);
                } else {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this,
                            "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Login réseau: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this,
                        "Erreur réseau: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── Step 2 : Récupération du profil Patient ───────────────────────────────

    private void fetchPatientProfile(User user) {
        ApiService api         = ApiClient.getClient().create(ApiService.class);
        String     bearerToken = "Bearer " + user.getToken();
        String     userId      = user.getUserId();

        Log.d(TAG, "fetchPatientProfile pour userId=" + userId);

        api.getPatientByUserId(bearerToken, userId).enqueue(new Callback<Patient>() {
            @Override
            public void onResponse(Call<Patient> call, Response<Patient> response) {
                setLoading(false);
                Log.d(TAG, "getPatientByUserId HTTP: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Patient patient = response.body();
                    Log.d(TAG, "Patient reçu: " + patient.getFirstName()
                            + " " + patient.getLastName()
                            + " tel=" + patient.getPhoneNumber()
                            + " dob=" + patient.getDateOfBirth());

                    // ✅ Fusion intelligente : API > local si non vide
                    mergeAndSavePatientData(patient);

                    String displayName = patient.getFullName();
                    if (displayName.trim().isEmpty()) displayName = user.getEmail();

                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putString("userName", displayName)
                            .apply();

                    Toast.makeText(LoginActivity.this,
                            "Bienvenue " + displayName, Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "Pas de profil patient pour userId=" + userId
                            + " HTTP " + response.code());
                    // Pas d'écrasement — les données locales (Register) sont conservées
                    Toast.makeText(LoginActivity.this,
                            "Bienvenue " + user.getEmail(), Toast.LENGTH_SHORT).show();
                }
                goHome();
            }

            @Override
            public void onFailure(Call<Patient> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "fetchPatientProfile réseau: " + t.getMessage(), t);
                // Pas d'écrasement en cas d'erreur réseau
                Toast.makeText(LoginActivity.this,
                        "Bienvenue " + user.getEmail(), Toast.LENGTH_SHORT).show();
                goHome();
            }
        });
    }

    // ── Sauvegarde auth de base ───────────────────────────────────────────────

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

    /**
     * Fusion intelligente : pour chaque champ, on prend la valeur API
     * seulement si elle est non nulle et non vide.
     * Sinon on garde la valeur déjà présente dans SharedPreferences
     * (sauvegardée par RegisterActivity lors de l'inscription).
     *
     * Ainsi les données du formulaire d'inscription ne sont jamais perdues
     * si l'API ne les renvoie pas encore.
     */
    private void mergeAndSavePatientData(Patient patient) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Lecture des valeurs actuelles (données Register)
        String localFirstName      = prefs.getString("userFirstName",      "");
        String localLastName       = prefs.getString("userLastName",       "");
        String localPhone          = prefs.getString("userPhone",          "");
        String localDob            = prefs.getString("userDob",            "");
        String localGender         = prefs.getString("userGender",         "");
        String localAddress        = prefs.getString("userAddress",        "");
        String localBloodType      = prefs.getString("userBloodType",      "");
        String localAllergies      = prefs.getString("userAllergies",      "");
        String localMedicalHistory = prefs.getString("userMedicalHistory", "");

        // Fusion : API gagne si non vide, sinon on garde le local
        String firstName      = pick(patient.getFirstName(),     localFirstName);
        String lastName       = pick(patient.getLastName(),      localLastName);
        String phone          = pick(patient.getPhoneNumber(),   localPhone);
        String dob            = pick(patient.getDateOfBirth(),   localDob);
        String gender         = pick(patient.getGender(),        localGender);
        String address        = pick(patient.getAddress(),       localAddress);
        String bloodType      = pick(patient.getBloodType(),     localBloodType);
        String allergies      = pick(patient.getAllergies(),      localAllergies);
        String medicalHistory = pick(patient.getMedicalHistory(),localMedicalHistory);

        int consultationCount = patient.getConsultationCount();

        Log.d(TAG, "Merge résultat → firstName=" + firstName
                + " lastName=" + lastName
                + " phone=" + phone
                + " dob=" + dob
                + " gender=" + gender);

        prefs.edit()
                .putString("patientId",          String.valueOf(patient.getId()))
                .putString("userFirstName",       firstName)
                .putString("userLastName",        lastName)
                .putString("userPhone",           phone)
                .putString("userDob",             dob)
                .putString("userGender",          gender)
                .putString("userAddress",         address)
                .putString("userBloodType",       bloodType)
                .putString("userAllergies",       allergies)
                .putString("userMedicalHistory",  medicalHistory)
                .putInt   ("consultationCount",   consultationCount)
                .apply();
    }

    /**
     * Retourne apiValue si non nulle et non vide, sinon localValue.
     */
    private String pick(String apiValue, String localValue) {
        return (apiValue != null && !apiValue.trim().isEmpty())
                ? apiValue.trim()
                : localValue;
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    private void goHome() {
        startActivity(new Intent(this, ActivityHome.class));
        finish();
    }

    private void setLoading(boolean loading) {
        if (progressBar != null)
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Connexion…" : "Se connecter");
    }

    private String text(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }
}