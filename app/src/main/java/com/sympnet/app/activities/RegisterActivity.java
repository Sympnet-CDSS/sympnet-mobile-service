package com.sympnet.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sympnet.app.R;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://faster-say-trimmer.ngrok-free.dev"; // ← ngrok URL

    // Step 1
    private EditText etFirstName, etLastName, etEmail, etPhone;
    // Step 2
    private EditText etDob, etAllergies;
    private Spinner spinnerGender, spinnerBlood, spinnerCity;
    // Step 3
    private EditText etPassword, etConfirmPassword;
    // Step 4
    private EditText etVerificationCode;

    private Button btnRegister;
    private int step = 1;

    // Données temporaires à garder pour EditProfile après login
    private String tempFirstName, tempLastName, tempPhone, tempDob,
            tempGender, tempBloodType, tempCity, tempAllergies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFirstName        = findViewById(R.id.et_first_name);
        etLastName         = findViewById(R.id.et_last_name);
        etEmail            = findViewById(R.id.et_email);
        etPhone            = findViewById(R.id.et_phone);
        etDob              = findViewById(R.id.et_dob);
        etAllergies        = findViewById(R.id.et_allergies);
        etPassword         = findViewById(R.id.et_password);
        etConfirmPassword  = findViewById(R.id.et_confirm_password);
        etVerificationCode = findViewById(R.id.et_verification_code);
        btnRegister        = findViewById(R.id.btn_register);
        spinnerGender      = findViewById(R.id.spinner_gender);
        spinnerBlood       = findViewById(R.id.spinner_blood_type);
        spinnerCity        = findViewById(R.id.spinner_city);

        spinnerGender.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Select Gender", "Male", "Female"}) {{
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }});

        spinnerBlood.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Select Blood Type", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"}) {{
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }});

        spinnerCity.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        "Select City", "Tunis", "Sfax", "Sousse", "Kairouan", "Bizerte",
                        "Gabès", "Ariana", "Gafsa", "Monastir", "Ben Arous",
                        "Kasserine", "Médenine", "Nabeul", "Tataouine", "Béja",
                        "Jendouba", "Mahdia", "Siliana", "Zaghouan", "Tozeur",
                        "Manouba", "Kébili", "Kef", "Sidi Bouzid"
                }) {{
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }});

        etDob.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) ->
                    etDob.setText(String.format("%02d/%02d/%04d", day, month + 1, year)),
                    cal.get(Calendar.YEAR) - 20,
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        findViewById(R.id.tv_resend).setOnClickListener(v -> callRegisterApi());

        findViewById(R.id.tv_login).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        showStep(1);

        btnRegister.setOnClickListener(v -> {
            if      (step == 1 && validateStep1()) showStep(2);
            else if (step == 2 && validateStep2()) showStep(3);
            else if (step == 3 && validateStep3()) callRegisterApi();
            else if (step == 4)                    callVerifyCodeApi();
        });
    }

    // ── API Register ──────────────────────────────────────────────────────

    private void callRegisterApi() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = (etFirstName.getText().toString().trim()
                + " " + etLastName.getText().toString().trim()).trim();

        // Sauvegarde temporaire des données du formulaire
        tempFirstName = etFirstName.getText().toString().trim();
        tempLastName  = etLastName.getText().toString().trim();
        tempPhone     = etPhone.getText().toString().trim();
        tempDob       = etDob.getText().toString().trim();
        tempGender    = spinnerGender.getSelectedItem().toString();
        tempBloodType = spinnerBlood.getSelectedItem().toString();
        tempCity      = spinnerCity.getSelectedItem().toString();
        tempAllergies = etAllergies.getText().toString().trim();

        btnRegister.setEnabled(false);

        try {
            JSONObject body = new JSONObject();
            body.put("email",    email);
            body.put("password", password);
            body.put("fullName", fullName);
            body.put("role",     "Patient");

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"), body.toString());

            Request request = new Request.Builder()
                    .url(BASE_URL + "/api/auth/register-mobile")
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .post(requestBody)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this,
                                "Erreur réseau : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Code envoyé à " + email, Toast.LENGTH_SHORT).show();
                            showStep(4);
                        } else {
                            try {
                                String msg = new JSONObject(responseBody).optString("message", "Erreur");
                                Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                            } catch (Exception ex) {
                                Toast.makeText(RegisterActivity.this, responseBody, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });

        } catch (Exception e) {
            btnRegister.setEnabled(true);
            Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ── API Verify Code ───────────────────────────────────────────────────

    private void callVerifyCodeApi() {
        String email = etEmail.getText().toString().trim();
        String code  = etVerificationCode.getText().toString().trim();

        if (code.isEmpty()) {
            Toast.makeText(this, "Enter the verification code", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("code",  code);

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"), body.toString());

            Request request = new Request.Builder()
                    .url(BASE_URL + "/api/auth/verify-code")
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .post(requestBody)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this,
                                "Erreur réseau : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        if (response.isSuccessful()) {
                            // ✅ Sauvegarde les données du formulaire dans SharedPreferences
                            // pour que EditProfile les affiche après le premier login
                            saveFormDataLocally();

                            Toast.makeText(RegisterActivity.this,
                                    "Account verified! Please sign in. ✅",
                                    Toast.LENGTH_LONG).show();

                            // ✅ Redirige vers Login (pas Home)
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {
                            try {
                                String msg = new JSONObject(responseBody).optString("message", "Code invalide");
                                Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                            } catch (Exception ex) {
                                Toast.makeText(RegisterActivity.this, responseBody, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });

        } catch (Exception e) {
            btnRegister.setEnabled(true);
            Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ── Sauvegarde locale des données du formulaire ───────────────────────
    // Ces données seront disponibles dans EditProfile dès le premier login
    private void saveFormDataLocally() {
        getSharedPreferences("SympNetPrefs", MODE_PRIVATE).edit()
                .putString("userFirstName", tempFirstName)
                .putString("userLastName",  tempLastName)
                .putString("userPhone",     tempPhone)
                .putString("userDob",       tempDob)
                .putString("userGender",    tempGender)
                .putString("userBloodType", tempBloodType)
                .putString("userAddress",   tempCity)
                .putString("userAllergies", tempAllergies)
                .apply();
    }

    // ── Step navigation ───────────────────────────────────────────────────

    private void showStep(int newStep) {
        step = newStep;
        findViewById(R.id.step1_layout).setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.step2_layout).setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        findViewById(R.id.step3_layout).setVisibility(step == 3 ? View.VISIBLE : View.GONE);
        findViewById(R.id.step4_layout).setVisibility(step == 4 ? View.VISIBLE : View.GONE);

        findViewById(R.id.dot1).setBackgroundResource(step == 1 ? R.drawable.dot_active : R.drawable.dot_inactive);
        findViewById(R.id.dot2).setBackgroundResource(step == 2 ? R.drawable.dot_active : R.drawable.dot_inactive);
        findViewById(R.id.dot3).setBackgroundResource(step == 3 ? R.drawable.dot_active : R.drawable.dot_inactive);
        findViewById(R.id.dot4).setBackgroundResource(step == 4 ? R.drawable.dot_active : R.drawable.dot_inactive);

        switch (step) {
            case 1: btnRegister.setText("Next →"); break;
            case 2: btnRegister.setText("Next →"); break;
            case 3: btnRegister.setText("Create account →"); break;
            case 4: btnRegister.setText("Verify & Continue →"); break;
        }
    }

    // ── Validation ────────────────────────────────────────────────────────

    private boolean validateStep1() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName  = etLastName.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        if (etDob.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select your date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerGender.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerBlood.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select your blood type", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerCity.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select your city", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateStep3() {
        String password = etPassword.getText().toString().trim();
        String confirm  = etConfirmPassword.getText().toString().trim();

        if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}