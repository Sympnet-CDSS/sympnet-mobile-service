package com.sympnet.app.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;
import com.sympnet.app.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    // ── Views ────────────────────────────────────────────────────────────────
    private ImageView        ivProfileImage;
    private TextView         tvProfileName, tvProfileEmail;
    private TextInputEditText etFirstName, etLastName, etEmail, etPhone,
            etDob, etGender, etBloodType, etAddress,
            etAllergies, etMedicalHistory, etConsultationCount;

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

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        bindViews();
        loadRealUsername();   // ← affiche le vrai nom depuis SharedPreferences
        loadProfilePhoto();   // ← charge la photo sauvegardée (ou l'avatar par défaut)
        prefillFields();
        setupListeners();
    }

    // ── 1. Bind views ────────────────────────────────────────────────────────
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

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    // ── 2. Afficher le vrai nom (depuis SharedPreferences) ───────────────────
    /**
     * Priorité :
     *  1. userName (fullName sauvegardé lors du dernier saveChanges)
     *  2. userFirstName + userLastName (si userName absent)
     *  3. "Utilisateur" comme fallback
     */
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

    // ── 3. Charger la photo de profil ────────────────────────────────────────
    private void loadProfilePhoto() {
        String base64 = prefs.getString(KEY_PHOTO, null);
        if (base64 != null) {
            Bitmap bmp = base64ToBitmap(base64);
            if (bmp != null) {
                Glide.with(this)
                        .load(bmp)
                        .transform(new CircleCrop())
                        .into(ivProfileImage);
                return;
            }
        }
        // Pas de photo sauvegardée → avatar par défaut
        Glide.with(this)
                .load(R.drawable.ic_profile_avatar)
                .transform(new CircleCrop())
                .into(ivProfileImage);
    }

    // ── 4. Pré-remplir les champs ────────────────────────────────────────────
    private void prefillFields() {
        setText(etFirstName,        prefs.getString("userFirstName",      ""));
        setText(etLastName,         prefs.getString("userLastName",       ""));
        setText(etEmail,            prefs.getString("userEmail",          ""));
        setText(etPhone,            prefs.getString("userPhone",          ""));
        setText(etDob,              prefs.getString("userDob",            ""));
        setText(etGender,           prefs.getString("userGender",         ""));
        setText(etBloodType,        prefs.getString("userBloodType",      ""));
        setText(etAddress,          prefs.getString("userAddress",        ""));
        setText(etAllergies,        prefs.getString("userAllergies",      ""));
        setText(etMedicalHistory,   prefs.getString("userMedicalHistory", ""));
        setText(etConsultationCount,
                String.valueOf(prefs.getInt("consultationCount", 0)));
    }

    // ── 5. Listeners ─────────────────────────────────────────────────────────
    private void setupListeners() {
        // Bouton caméra
        LinearLayout btnChangePhoto = findViewById(R.id.btnChangePhoto);
        if (btnChangePhoto != null)
            btnChangePhoto.setOnClickListener(v -> requestGalleryPermission());

        // Clic sur l'avatar lui-même (même action)
        if (ivProfileImage != null)
            ivProfileImage.setOnClickListener(v -> requestGalleryPermission());

        // Lien "Changer la photo de profil" (TextView cliquable dans le layout)
        TextView tvChangePic = findViewById(R.id.tvChangePhoto);
        if (tvChangePic != null)
            tvChangePic.setOnClickListener(v -> requestGalleryPermission());

        // Sauvegarder
        Button btnSave = findViewById(R.id.btnSaveProfile);
        btnSave.setOnClickListener(v -> saveChanges());
    }

    // ── 6. Permission + galerie ───────────────────────────────────────────────
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

    // ── 7. Traitement de l'image sélectionnée ────────────────────────────────
    private void handleSelectedImage(Uri uri) {
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(stream);
            if (original == null) return;

            // Redimensionner à 300×300 max pour économiser la mémoire
            Bitmap resized = scaleBitmap(original, 300);

            // Afficher immédiatement (cercle via Glide)
            Glide.with(this)
                    .load(resized)
                    .transform(new CircleCrop())
                    .into(ivProfileImage);

            // Persister en Base64 dans SharedPreferences
            String base64 = bitmapToBase64(resized);
            prefs.edit().putString(KEY_PHOTO, base64).apply();

            Toast.makeText(this, "Photo mise à jour ✓", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Impossible de charger l'image", Toast.LENGTH_SHORT).show();
        }
    }

    // ── 8. Sauvegarder les modifications ─────────────────────────────────────
    private void saveChanges() {
        String firstName = text(etFirstName);
        String lastName  = text(etLastName);

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Prénom et nom requis", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = (firstName + " " + lastName).trim();

        // Mettre à jour l'en-tête en temps réel
        tvProfileName.setText(fullName);

        prefs.edit()
                .putString("userName",          fullName)
                .putString("userFirstName",     firstName)
                .putString("userLastName",      lastName)
                .putString("userPhone",         text(etPhone))
                .putString("userDob",           text(etDob))
                .putString("userGender",        text(etGender))
                .putString("userBloodType",     text(etBloodType))
                .putString("userAddress",       text(etAddress))
                .putString("userAllergies",     text(etAllergies))
                .putString("userMedicalHistory",text(etMedicalHistory))
                .apply();

        Toast.makeText(this, "Profil mis à jour ✓", Toast.LENGTH_SHORT).show();
        finish(); // ProfileFragment se rafraîchit dans onResume()
    }

    // ── Helpers image ─────────────────────────────────────────────────────────

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

    // ── Helpers champs ────────────────────────────────────────────────────────

    private void setText(TextInputEditText field, String value) {
        if (field != null) field.setText(value);
    }

    private String text(TextInputEditText field) {
        return (field != null && field.getText() != null)
                ? field.getText().toString().trim()
                : "";
    }
}