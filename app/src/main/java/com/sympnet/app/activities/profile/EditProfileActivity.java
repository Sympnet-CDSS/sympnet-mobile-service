package com.sympnet.app.activities.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.sympnet.app.R;
import com.sympnet.app.model.Patient;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    // ── Views ────────────────────────────────────────────────────────────────
    private ImageView        ivProfileImage;
    private TextView         tvProfileName, tvProfileEmail;
    private EditText etFirstName, etLastName, etEmail, etPhone,
            etDob, etGender, etBloodType, etAddress,
            etAllergies, etMedicalHistory, etConsultationCount;
    private Button           btnSaveProfile;
    private TextView         btnGenderMale, btnGenderFemale, btnGenderOther;
    private TextView         tvConsultationCount;

    // ── State ────────────────────────────────────────────────────────────────
    private SharedPreferences prefs;
    private static final String PREFS_NAME   = "SympNetPrefs";
    private static final String KEY_PHOTO    = "userPhotoBase64";  // stored as Base64 string

    // ── Photo picker launcher ────────────────────────────────────────────────
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri != null) handleSelectedImage(uri);
                        }
                    });

    // ── Permission launcher (Android 13+) ────────────────────────────────────
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) openGallery();
                        else Toast.makeText(this,
                                "Permission refusée — impossible d'accéder à la galerie",
                                Toast.LENGTH_SHORT).show();
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        bindViews();
        loadLocalData();       // Prefill local cache immediately
        setupListeners();
        fetchPatientProfile();  // Retrieve actual data from the server in real-time
    }

    private void bindViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvProfileName  = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        etFirstName         = findViewById(R.id.etFirstName);
        etLastName          = findViewById(R.id.etLastName);
        etEmail             = findViewById(R.id.etEmail);
        etPhone             = findViewById(R.id.etPhone);
        etDob               = findViewById(R.id.etDob);
        etGender            = findViewById(R.id.etGender);
        etBloodType         = findViewById(R.id.etBloodType);
        etAddress           = findViewById(R.id.etAddress);
        etAllergies         = findViewById(R.id.etAllergies);
        etMedicalHistory    = findViewById(R.id.etMedicalHistory);
        etConsultationCount = findViewById(R.id.etConsultationCount);

        btnSaveProfile      = findViewById(R.id.btnSaveProfile);

        btnGenderMale       = findViewById(R.id.btnGenderMale);
        btnGenderFemale     = findViewById(R.id.btnGenderFemale);
        btnGenderOther      = findViewById(R.id.btnGenderOther);
        tvConsultationCount = findViewById(R.id.tvConsultationCount);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void loadLocalData() {
        loadRealUsername();
        loadProfilePhoto();
        prefillFields();
    }

    private void loadRealUsername() {
        String fullName = prefs.getString("userName", "").trim();

        if (fullName.isEmpty()) {
            String first = prefs.getString("userFirstName", "").trim();
            String last  = prefs.getString("userLastName",  "").trim();
            fullName = (first + " " + last).trim();
        }

        if (fullName.isEmpty()) fullName = "Utilisateur";

        tvProfileName.setText(fullName);
        tvProfileEmail.setText(prefs.getString("userEmail", ""));
    }

    private void loadProfilePhoto() {
        String base64 = prefs.getString(KEY_PHOTO, null);
        if (base64 != null && !base64.isEmpty()) {
            Bitmap bmp = base64ToBitmap(base64);
            if (bmp != null) {
                Glide.with(this)
                        .load(bmp)
                        .transform(new CircleCrop())
                        .into(ivProfileImage);
                return;
            }
        }
        Glide.with(this)
                .load(R.drawable.ic_profile_avatar)
                .transform(new CircleCrop())
                .into(ivProfileImage);
    }

    private void prefillFields() {
        setText(etFirstName,        prefs.getString("userFirstName",      ""));
        setText(etLastName,         prefs.getString("userLastName",       ""));
        setText(etEmail,            prefs.getString("userEmail",          ""));
        setText(etPhone,            prefs.getString("userPhone",          ""));
        
        String dob = prefs.getString("userDob", "");
        setText(etDob,              formatDateForDisplay(dob));
        
        String gender = prefs.getString("userGender", "");
        setText(etGender,           gender);
        selectGender(gender);

        setText(etBloodType,        prefs.getString("userBloodType",      ""));
        setText(etAddress,          prefs.getString("userAddress",        ""));
        setText(etAllergies,        prefs.getString("userAllergies",      ""));
        setText(etMedicalHistory,   prefs.getString("userMedicalHistory", ""));
        
        int consultationCount = prefs.getInt("consultationCount", 0);
        setText(etConsultationCount, String.valueOf(consultationCount));
        if (tvConsultationCount != null) {
            tvConsultationCount.setText(String.valueOf(consultationCount));
        }
    }

    private void fetchPatientProfile() {
        String userId = prefs.getString("userId", "");
        String token  = prefs.getString("userToken", "");
        if (userId.isEmpty() || token.isEmpty()) return;

        setLoading(true, "Chargement...");
        String bearerToken = "Bearer " + token;
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getPatientByUserId(bearerToken, userId).enqueue(new Callback<Patient>() {
            @Override
            public void onResponse(@NonNull Call<Patient> call, @NonNull Response<Patient> response) {
                setLoading(false, "Enregistrer les modifications");
                if (response.isSuccessful() && response.body() != null) {
                    Patient patient = response.body();
                    
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("userName",          patient.getFullName());
                    editor.putString("userFirstName",     patient.getFirstName());
                    editor.putString("userLastName",      patient.getLastName());
                    editor.putString("userPhone",         patient.getPhoneNumber());
                    editor.putString("userDob",           patient.getDateOfBirth());
                    editor.putString("userGender",        patient.getGender());
                    editor.putString("userBloodType",     patient.getBloodType());
                    editor.putString("userAddress",       patient.getAddress());
                    editor.putString("userAllergies",     patient.getAllergies());
                    editor.putString("userMedicalHistory",patient.getMedicalHistory());
                    editor.putInt("consultationCount",    patient.getConsultationCount());
                    
                    if (patient.getPhotoUrl() != null && !patient.getPhotoUrl().isEmpty()) {
                        editor.putString(KEY_PHOTO, patient.getPhotoUrl());
                    }
                    editor.apply();

                    loadLocalData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Patient> call, @NonNull Throwable t) {
                setLoading(false, "Enregistrer les modifications");
                Log.w(TAG, "Utilisation silencieuse des données en cache local", t);
            }
        });
    }

    private void setupListeners() {
        LinearLayout btnChangePhoto = findViewById(R.id.btnChangePhoto);
        if (btnChangePhoto != null)
            btnChangePhoto.setOnClickListener(v -> requestGalleryPermission());

        if (ivProfileImage != null)
            ivProfileImage.setOnClickListener(v -> requestGalleryPermission());

        TextView tvChangePic = findViewById(R.id.tvChangePhoto);
        if (tvChangePic != null)
            tvChangePic.setOnClickListener(v -> requestGalleryPermission());

        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> saveChanges());
        }

        if (btnGenderMale != null) {
            btnGenderMale.setOnClickListener(v -> selectGender("Homme"));
        }
        if (btnGenderFemale != null) {
            btnGenderFemale.setOnClickListener(v -> selectGender("Femme"));
        }
        if (btnGenderOther != null) {
            btnGenderOther.setOnClickListener(v -> selectGender("Autre"));
        }

        if (etDob != null) {
            etDob.setFocusable(false);
            etDob.setClickable(true);
            etDob.setOnClickListener(v -> showDatePicker());
        }
    }

    private void showDatePicker() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        String currentDob = text(etDob);
        if (!currentDob.isEmpty() && currentDob.contains("/")) {
            String[] parts = currentDob.split("/");
            if (parts.length == 3) {
                try {
                    day = Integer.parseInt(parts[0]);
                    month = Integer.parseInt(parts[1]) - 1;
                    year = Integer.parseInt(parts[2]);
                } catch (Exception ignored) {}
            }
        }

        android.app.DatePickerDialog datePicker = new android.app.DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    if (etDob != null) etDob.setText(formattedDate);
                },
                year, month, day
        );
        datePicker.show();
    }

    private void requestGalleryPermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            permissionLauncher.launch(permission);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleSelectedImage(Uri uri) {
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(stream);
            if (original == null) return;

            Bitmap resized = scaleBitmap(original, 300);

            Glide.with(this)
                    .load(resized)
                    .transform(new CircleCrop())
                    .into(ivProfileImage);

            String base64 = bitmapToBase64(resized);
            prefs.edit().putString(KEY_PHOTO, base64).apply();

            Toast.makeText(this, "Photo mise à jour ✓ (Enregistrez pour synchroniser)", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Impossible de charger l'image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveChanges() {
        String firstName = text(etFirstName);
        String lastName  = text(etLastName);

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Prénom et nom requis", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = (firstName + " " + lastName).trim();
        tvProfileName.setText(fullName);

        String userId = prefs.getString("userId", "");
        String token  = prefs.getString("userToken", "");
        if (userId.isEmpty() || token.isEmpty()) {
            saveLocalAndFinish(firstName, lastName, fullName);
            return;
        }

        setLoading(true, "Enregistrement...");
        String bearerToken = "Bearer " + token;
        ApiService api = ApiClient.getClient().create(ApiService.class);

        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("phoneNumber", text(etPhone));
        body.put("dateOfBirth", formatDateForServer(text(etDob)));
        body.put("gender", text(etGender));
        body.put("bloodType", text(etBloodType));
        body.put("address", text(etAddress));
        body.put("medicalHistory", text(etMedicalHistory));

        String base64Photo = prefs.getString(KEY_PHOTO, null);
        if (base64Photo != null && !base64Photo.isEmpty()) {
            body.put("photoUrl", base64Photo);
        }

        String allergiesText = text(etAllergies);
        if (!allergiesText.isEmpty()) {
            body.put("allergies", Arrays.asList(allergiesText.split("\\s*,\\s*")));
        } else {
            body.put("allergies", Arrays.asList());
        }

        api.updatePatient(bearerToken, userId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                setLoading(false, "Enregistrer les modifications");
                if (response.isSuccessful()) {
                    saveLocalAndFinish(firstName, lastName, fullName);
                } else {
                    Log.e(TAG, "Erreur serveur lors de la sauvegarde: " + response.code());
                    Toast.makeText(EditProfileActivity.this, "Erreur serveur (" + response.code() + ") lors de la sauvegarde", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                setLoading(false, "Enregistrer les modifications");
                Log.e(TAG, "Échec réseau", t);
                Toast.makeText(EditProfileActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLocalAndFinish(String firstName, String lastName, String fullName) {
        prefs.edit()
                .putString("userName",          fullName)
                .putString("userFirstName",     firstName)
                .putString("userLastName",      lastName)
                .putString("userPhone",         text(etPhone))
                .putString("userDob",           formatDateForServer(text(etDob)))
                .putString("userGender",        text(etGender))
                .putString("userBloodType",     text(etBloodType))
                .putString("userAddress",       text(etAddress))
                .putString("userAllergies",     text(etAllergies))
                .putString("userMedicalHistory",text(etMedicalHistory))
                .apply();

        Toast.makeText(this, "Profil mis à jour avec succès ✓", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setLoading(boolean loading, String buttonText) {
        if (btnSaveProfile != null) {
            btnSaveProfile.setEnabled(!loading);
            btnSaveProfile.setText(buttonText);
        }
    }

    private String formatDateForDisplay(String dob) {
        if (dob == null || dob.isEmpty()) return "";
        if (dob.contains("T")) {
            dob = dob.split("T")[0];
        }
        if (dob.contains("-")) {
            String[] parts = dob.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        }
        return dob;
    }

    private String formatDateForServer(String dob) {
        if (dob == null || dob.isEmpty()) return null;
        if (dob.contains("/")) {
            String[] parts = dob.split("/");
            if (parts.length == 3) {
                return parts[2] + "-" + parts[1] + "-" + parts[0];
            }
        }
        return dob;
    }

    private Bitmap scaleBitmap(Bitmap src, int maxPx) {
        int w = src.getWidth(), h = src.getHeight();
        if (w <= maxPx && h <= maxPx) return src;
        float ratio = Math.min((float) maxPx / w, (float) maxPx / h);
        return Bitmap.createScaledBitmap(src, (int)(w * ratio), (int)(h * ratio), true);
    }

    private String bitmapToBase64(Bitmap bmp) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private void setText(EditText field, String value) {
        if (field != null) field.setText(value);
    }

    private String text(EditText field) {
        return (field != null && field.getText() != null)
                ? field.getText().toString().trim()
                : "";
    }

    private void selectGender(String gender) {
        if (gender == null) gender = "";
        
        // Update hidden etGender
        if (etGender != null) {
            etGender.setText(gender);
        }

        // Reset all styles
        if (btnGenderMale != null) {
            btnGenderMale.setBackgroundResource(R.drawable.bg_gender_inactive);
            btnGenderMale.setTextColor(Color.parseColor("#333333"));
        }
        if (btnGenderFemale != null) {
            btnGenderFemale.setBackgroundResource(R.drawable.bg_gender_inactive);
            btnGenderFemale.setTextColor(Color.parseColor("#333333"));
        }
        if (btnGenderOther != null) {
            btnGenderOther.setBackgroundResource(R.drawable.bg_gender_inactive);
            btnGenderOther.setTextColor(Color.parseColor("#333333"));
        }

        // Apply active style
        if (gender.equalsIgnoreCase("Homme") || gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("M")) {
            if (btnGenderMale != null) {
                btnGenderMale.setBackgroundResource(R.drawable.bg_gender_active);
                btnGenderMale.setTextColor(Color.parseColor("#FFFFFF"));
            }
        } else if (gender.equalsIgnoreCase("Femme") || gender.equalsIgnoreCase("Female") || gender.equalsIgnoreCase("F")) {
            if (btnGenderFemale != null) {
                btnGenderFemale.setBackgroundResource(R.drawable.bg_gender_active);
                btnGenderFemale.setTextColor(Color.parseColor("#FFFFFF"));
            }
        } else {
            if (btnGenderOther != null) {
                btnGenderOther.setBackgroundResource(R.drawable.bg_gender_active);
                btnGenderOther.setTextColor(Color.parseColor("#FFFFFF"));
            }
        }
    }
}