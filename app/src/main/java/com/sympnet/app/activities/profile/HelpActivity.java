package com.sympnet.app.activities.profile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sympnet.app.R;

public class HelpActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPhone, etMessage;
    private Spinner spinnerSubject;
    private CheckBox cbAcceptConditions;
    private MaterialButton btnSubmitHelp;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Bind Views
        btnBack = findViewById(R.id.btnBack);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        etMessage = findViewById(R.id.etMessage);
        cbAcceptConditions = findViewById(R.id.cbAcceptConditions);
        btnSubmitHelp = findViewById(R.id.btnSubmitHelp);

        // Back button action
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Setup Spinner
        String[] subjects = new String[]{
            "Sélectionnez le domaine...",
            "Support Technique",
            "Facturation & Paiement",
            "Rendez-vous & Consultations",
            "Autre"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this,
            R.layout.spinner_item,
            subjects
        ) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item 
                return position != 0;
            }
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                android.widget.TextView tv = (android.widget.TextView) view;
                if (position == 0) {
                    tv.setTextColor(android.graphics.Color.GRAY);
                } else {
                    tv.setTextColor(android.graphics.Color.BLACK);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        if (spinnerSubject != null) {
            spinnerSubject.setAdapter(adapter);
        }

        // Pre-populate fields from SharedPreferences
        prepopulateFields();

        // Submit action
        if (btnSubmitHelp != null) {
            btnSubmitHelp.setOnClickListener(v -> handleSubmit());
        }
    }

    private void prepopulateFields() {
        SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        String name = prefs.getString("userName", "");
        String email = prefs.getString("userEmail", "");
        String phone = prefs.getString("userPhone", "");

        if (!name.isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                if (etFirstName != null) etFirstName.setText(parts[0]);
                StringBuilder lastNameBuilder = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    if (i > 1) lastNameBuilder.append(" ");
                    lastNameBuilder.append(parts[i]);
                }
                if (etLastName != null) etLastName.setText(lastNameBuilder.toString());
            } else if (parts.length == 1) {
                if (etFirstName != null) etFirstName.setText(parts[0]);
            }
        }

        if (!email.isEmpty() && etEmail != null) {
            etEmail.setText(email);
        }

        if (!phone.isEmpty() && !phone.equals("+216 20 123 456") && etPhone != null) {
            etPhone.setText(phone);
        }
    }

    private void handleSubmit() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        int subjectPos = spinnerSubject.getSelectedItemPosition();
        String selectedSubject = spinnerSubject.getSelectedItem().toString();

        if (firstName.isEmpty()) {
            etFirstName.setError("Prénom obligatoire");
            etFirstName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Nom obligatoire");
            etLastName.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email invalide");
            etEmail.requestFocus();
            return;
        }

        if (subjectPos == 0) {
            Toast.makeText(this, "Veuillez choisir un sujet", Toast.LENGTH_LONG).show();
            spinnerSubject.requestFocus();
            return;
        }

        if (message.isEmpty()) {
            etMessage.setError("Message obligatoire");
            etMessage.requestFocus();
            return;
        }

        if (!cbAcceptConditions.isChecked()) {
            Toast.makeText(this, "Veuillez accepter les conditions pour continuer", Toast.LENGTH_LONG).show();
            cbAcceptConditions.requestFocus();
            return;
        }

        
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Envoi en cours...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Créer l'objet de requête pour l'API
        com.sympnet.app.api.ContactService.ContactMessageRequest request = 
            new com.sympnet.app.api.ContactService.ContactMessageRequest(
                firstName,
                lastName,
                email,
                phone,
                selectedSubject,
                message
            );

        // Envoyer la réclamation via Retrofit
        com.sympnet.app.api.ContactService service = 
            com.sympnet.app.api.RetrofitClient.getInstance().create(com.sympnet.app.api.ContactService.class);

        service.createContactMessage(request).enqueue(new retrofit2.Callback<com.sympnet.app.api.ContactService.ContactResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.sympnet.app.api.ContactService.ContactResponse> call, 
                                   retrofit2.Response<com.sympnet.app.api.ContactService.ContactResponse> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (response.isSuccessful()) {
                    // Afficher un dialogue de succès avec confirmation de transmission à l'admin
                    new MaterialAlertDialogBuilder(HelpActivity.this)
                            .setTitle("Message envoyé avec succès !")
                            .setMessage("Merci " + firstName + " ! Votre message concernant le sujet \"" + selectedSubject + "\" a bien été transmis à l'administration de SympNet.\n\nNotre équipe administrative vous répondra par email dans les plus brefs délais.")
                            .setPositiveButton("Fermer", (dialog, which) -> finish())
                            .setCancelable(false)
                            .show();
                } else {
                    Toast.makeText(HelpActivity.this, "Erreur serveur : " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.sympnet.app.api.ContactService.ContactResponse> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(HelpActivity.this, "Erreur de connexion : " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
