package com.sympnet.mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.mobile.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPhone, etDob, etBloodType, etCity, etAllergies, etPassword, etConfirmPassword;
    private Button btnRegister;
    private int step = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etDob = findViewById(R.id.et_dob);
        etBloodType = findViewById(R.id.et_blood_type);
        etCity = findViewById(R.id.et_city);
        etAllergies = findViewById(R.id.et_allergies);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);

        showStep1();

        btnRegister.setOnClickListener(v -> {
            if (step == 1) {
                if (validateStep1()) {
                    step = 2;
                    showStep2();
                }
            } else if (step == 2) {
                if (validateStep2()) {
                    step = 3;
                    showStep3();
                }
            } else {
                if (validateStep3()) {
                    Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void showStep1() {
        findViewById(R.id.step1_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.step2_layout).setVisibility(View.GONE);
        findViewById(R.id.step3_layout).setVisibility(View.GONE);
        btnRegister.setText("Étape suivante →");
    }

    private void showStep2() {
        findViewById(R.id.step1_layout).setVisibility(View.GONE);
        findViewById(R.id.step2_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.step3_layout).setVisibility(View.GONE);
        btnRegister.setText("Étape suivante →");
    }

    private void showStep3() {
        findViewById(R.id.step1_layout).setVisibility(View.GONE);
        findViewById(R.id.step2_layout).setVisibility(View.GONE);
        findViewById(R.id.step3_layout).setVisibility(View.VISIBLE);
        btnRegister.setText("Créer mon compte →");
    }

    private boolean validateStep1() {
        if (etFirstName.getText().toString().trim().isEmpty() ||
                etLastName.getText().toString().trim().isEmpty() ||
                etEmail.getText().toString().trim().isEmpty() ||
                etPhone.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        if (etDob.getText().toString().trim().isEmpty() ||
                etBloodType.getText().toString().trim().isEmpty() ||
                etCity.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateStep3() {
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (password.length() < 8) {
            Toast.makeText(this, "Mot de passe minimum 8 caractères", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
