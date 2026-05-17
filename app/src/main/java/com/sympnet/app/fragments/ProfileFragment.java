package com.sympnet.app.fragments;
import android.util.Log;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;
import com.sympnet.app.model.Patient;
import com.sympnet.app.activities.profile.PrivacyPolicyActivity;
import com.sympnet.app.activities.profile.HelpActivity;
import com.sympnet.app.activities.profile.SettingsActivity;
import com.sympnet.app.activities.profile.EditProfileActivity;
import com.sympnet.app.activities.doctor.FavoriteDoctorsActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sympnet.app.R;
import com.sympnet.app.activities.auth.LoginActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
public class ProfileFragment extends Fragment {

    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("SympNetPrefs", Context.MODE_PRIVATE);

        loadUserInfo(view);
        setupMenuClicks(view);
    }

    private void loadUserInfo(View view) {
        TextView tvName    = view.findViewById(R.id.tvUserName);
        TextView tvEmail   = view.findViewById(R.id.tvUserEmail);
        ImageView ivAvatar = view.findViewById(R.id.ivProfileAvatar);

        TextView tvInfoEmail    = view.findViewById(R.id.tvInfoEmail);
        TextView tvInfoPhone    = view.findViewById(R.id.tvInfoPhone);
        TextView tvInfoAddress  = view.findViewById(R.id.tvInfoAddress);
        TextView tvInfoBirthday = view.findViewById(R.id.tvInfoBirthday);

        // Activités récentes
        TextView tvAct1Title = view.findViewById(R.id.tvActivity1Title);
        TextView tvAct1Sub   = view.findViewById(R.id.tvActivity1Subtitle);
        TextView tvAct2Title = view.findViewById(R.id.tvActivity2Title);
        TextView tvAct2Sub   = view.findViewById(R.id.tvActivity2Subtitle);

        // 1. Charger immédiatement les données locales en cache pour un affichage instantané
        String cachedName     = prefs.getString("userName",       "");
        String cachedEmail    = prefs.getString("userEmail",      "");
        String cachedPhone    = prefs.getString("userPhone",      "");
        String cachedAddress  = prefs.getString("userAddress",    "");
        String cachedBirthday = prefs.getString("userBirthday",   "");
        String cachedBase64   = prefs.getString("userPhotoBase64", null);

        tvName.setText(cachedName.isEmpty() ? "Patient" : cachedName);
        tvEmail.setText(cachedEmail);
        if (tvInfoEmail != null) tvInfoEmail.setText(cachedEmail);
        if (tvInfoPhone != null) tvInfoPhone.setText(cachedPhone.isEmpty() ? "Non renseigné" : cachedPhone);
        if (tvInfoAddress != null) tvInfoAddress.setText(cachedAddress.isEmpty() ? "Non renseignée" : cachedAddress);
        if (tvInfoBirthday != null) tvInfoBirthday.setText(cachedBirthday.isEmpty() ? "Non renseignée" : cachedBirthday);

        if (cachedBase64 != null && !cachedBase64.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(cachedBase64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bmp != null) {
                    Glide.with(this)
                            .load(bmp)
                            .placeholder(R.drawable.ic_profile_avatar)
                            .into(ivAvatar);
                }
            } catch (Exception e) {
                Glide.with(this).load(R.drawable.ic_profile_avatar).into(ivAvatar);
            }
        } else {
            Glide.with(this).load(R.drawable.ic_profile_avatar).into(ivAvatar);
        }

        // 2. Requête API pour récupérer les VRAIES données fraîches depuis le serveur
        String token = prefs.getString("userToken", "");
        String userId = prefs.getString("userId", "");
        if (!token.isEmpty() && !userId.isEmpty()) {
            String bearerToken = "Bearer " + token;
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            apiService.getPatientByUserId(bearerToken, userId).enqueue(new Callback<Patient>() {
                @Override
                public void onResponse(@NonNull Call<Patient> call, @NonNull Response<Patient> response) {
                    if (isAdded() && response.isSuccessful() && response.body() != null) {
                        Patient patient = response.body();

                        // Formater et affecter les nouvelles données sur l'UI
                        String realName = patient.getFullName();
                        String realEmail = patient.getEmail();
                        String realPhone = patient.getPhoneNumber();
                        String realAddress = patient.getAddress();
                        String realDobFormatted = formatBirthDate(patient.getDateOfBirth());
                        String realPhoto = patient.getPhotoUrl();

                        tvName.setText(realName.isEmpty() ? "Patient" : realName);
                        tvEmail.setText(realEmail);
                        if (tvInfoEmail != null) tvInfoEmail.setText(realEmail);
                        if (tvInfoPhone != null) tvInfoPhone.setText(realPhone.isEmpty() ? "Non renseigné" : realPhone);
                        if (tvInfoAddress != null) tvInfoAddress.setText(realAddress.isEmpty() ? "Non renseignée" : realAddress);
                        if (tvInfoBirthday != null) tvInfoBirthday.setText(realDobFormatted);

                        if (realPhoto != null && !realPhoto.isEmpty()) {
                            try {
                                byte[] bytes = Base64.decode(realPhoto, Base64.DEFAULT);
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                if (bmp != null) {
                                    Glide.with(ProfileFragment.this)
                                            .load(bmp)
                                            .placeholder(R.drawable.ic_profile_avatar)
                                            .into(ivAvatar);
                                }
                            } catch (Exception e) {
                                // Fallback
                            }
                        }

                        // Enregistrer les modifications en cache local pour synchroniser le reste de l'application
                        prefs.edit()
                                .putString("userName", realName)
                                .putString("userEmail", realEmail)
                                .putString("userPhone", realPhone)
                                .putString("userAddress", realAddress)
                                .putString("userBirthday", realDobFormatted)
                                .putString("userPhotoBase64", realPhoto)
                                .apply();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Patient> call, @NonNull Throwable t) {
                    Log.e("ProfileFragment", "Erreur lors du chargement des données patient", t);
                }
            });
        }

        // Stats: Favoris
        SharedPreferences favPrefs = requireContext().getSharedPreferences("doctor_favorites", Context.MODE_PRIVATE);
        int favCount = favPrefs.getAll().size();
        TextView tvCountFav = view.findViewById(R.id.tvCountFav);
        TextView tvMenuFavBadge = view.findViewById(R.id.tvMenuFavCountBadge);
        TextView tvMenuFavText = view.findViewById(R.id.tvMenuFavCountText);
        
        if (tvCountFav != null) tvCountFav.setText(String.valueOf(favCount));
        if (tvMenuFavBadge != null) tvMenuFavBadge.setText(String.valueOf(favCount));
        if (tvMenuFavText != null) tvMenuFavText.setText(favCount + " médecins enregistrés");

        // 3. Charger les Rendez-vous et remplir dynamiquement l'activité récente
        TextView tvCountApp = view.findViewById(R.id.tvCountApp);
        if (!token.isEmpty()) {
            String bearerToken = "Bearer " + token;
            ApiClient.getClient().create(com.sympnet.app.api.AppointmentService.class)
                    .getMyAppointments(bearerToken).enqueue(new retrofit2.Callback<List<com.sympnet.app.model.AppointmentDto>>() {
                @Override
                public void onResponse(@NonNull Call<List<com.sympnet.app.model.AppointmentDto>> call,
                                       @NonNull Response<List<com.sympnet.app.model.AppointmentDto>> response) {
                    if (isAdded() && response.isSuccessful() && response.body() != null) {
                        List<com.sympnet.app.model.AppointmentDto> appointments = response.body();
                        
                        if (tvCountApp != null) {
                            tvCountApp.setText(String.valueOf(appointments.size()));
                        }

                        // Remplissage dynamique des deux lignes d'activité récente
                        int appSize = appointments.size();
                        if (appSize >= 1) {
                            com.sympnet.app.model.AppointmentDto app1 = appointments.get(0);
                            String statusLabel = "Confirmed".equalsIgnoreCase(app1.status) ? "confirmé" : "planifié";
                            if (tvAct1Title != null) tvAct1Title.setText("Rendez-vous " + statusLabel);
                            if (tvAct1Sub != null) tvAct1Sub.setText("Dr. " + app1.doctorName + " - " + formatAppointmentDateTime(app1.dateTime));
                        } else {
                            if (tvAct1Title != null) tvAct1Title.setText("Création du dossier médical");
                            if (tvAct1Sub != null) tvAct1Sub.setText("Dossier patient initialisé avec succès.");
                        }

                        if (appSize >= 2) {
                            com.sympnet.app.model.AppointmentDto app2 = appointments.get(1);
                            String statusLabel = "Confirmed".equalsIgnoreCase(app2.status) ? "confirmé" : "planifié";
                            if (tvAct2Title != null) tvAct2Title.setText("Rendez-vous " + statusLabel);
                            if (tvAct2Sub != null) tvAct2Sub.setText("Dr. " + app2.doctorName + " - " + formatAppointmentDateTime(app2.dateTime));
                        } else {
                            if (tvAct2Title != null) tvAct2Title.setText("Bienvenue sur SympNet");
                            if (tvAct2Sub != null) tvAct2Sub.setText("Votre application de santé intelligente.");
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<List<com.sympnet.app.model.AppointmentDto>> call, @NonNull Throwable t) {
                    if (tvCountApp != null) tvCountApp.setText("0");
                    if (tvAct1Title != null) tvAct1Title.setText("Création du dossier médical");
                    if (tvAct1Sub != null) tvAct1Sub.setText("Dossier patient initialisé avec succès.");
                    if (tvAct2Title != null) tvAct2Title.setText("Bienvenue sur SympNet");
                    if (tvAct2Sub != null) tvAct2Sub.setText("Votre application de santé intelligente.");
                }
            });
        }
    }

    private String formatBirthDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "Non renseignée";
        try {
            String clean = rawDate;
            if (rawDate.contains("T")) {
                clean = rawDate.split("T")[0];
            }
            String[] parts = clean.split("-");
            if (parts.length == 3) {
                String year = parts[0];
                String monthNum = parts[1];
                String day = parts[2];
                if (day.startsWith("0")) day = day.substring(1);
                
                String[] months = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
                int mIdx = Integer.parseInt(monthNum) - 1;
                if (mIdx >= 0 && mIdx < 12) {
                    return day + " " + months[mIdx] + " " + year;
                }
            }
            return clean;
        } catch (Exception e) {
            return rawDate;
        }
    }

    private String formatAppointmentDateTime(String rawDateTime) {
        if (rawDateTime == null || rawDateTime.length() < 16) return "";
        try {
            String dateClean = rawDateTime.split("T")[0];
            String timeClean = rawDateTime.split("T")[1].substring(0, 5);
            
            String[] parts = dateClean.split("-");
            String year = parts[0];
            String monthNum = parts[1];
            String day = parts[2];
            if (day.startsWith("0")) day = day.substring(1);
            
            String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
            int mIdx = Integer.parseInt(monthNum) - 1;
            String monthStr = (mIdx >= 0 && mIdx < 12) ? months[mIdx] : monthNum;
            
            return day + " " + monthStr + " " + year + " à " + timeClean;
        } catch (Exception e) {
            return rawDateTime;
        }
    }

    private void setupMenuClicks(View view) {
        View.OnClickListener editProfileListener = v -> {
            if (getActivity() != null) {
                startActivity(new Intent(requireActivity(), EditProfileActivity.class));
            }
        };

        View btnEdit = view.findViewById(R.id.btnEditProfile);
        if (btnEdit != null) btnEdit.setOnClickListener(editProfileListener);

        View ivEdit = view.findViewById(R.id.ivEditAvatar);
        if (ivEdit != null) ivEdit.setOnClickListener(editProfileListener);

        View.OnClickListener favListener = v -> {
            if (getActivity() != null) {
                startActivity(new Intent(requireActivity(), FavoriteDoctorsActivity.class));
            }
        };
        View btnFav = view.findViewById(R.id.btnGoFavorites);
        if (btnFav != null) btnFav.setOnClickListener(favListener);

        View btnPrivacy = view.findViewById(R.id.btnPrivacy);
        if (btnPrivacy != null) btnPrivacy.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(requireActivity(), PrivacyPolicyActivity.class));
            }
        });

        View btnNotifSettings = view.findViewById(R.id.btnNotificationsSettings);
        com.google.android.material.switchmaterial.SwitchMaterial switchNotifs = view.findViewById(R.id.switchNotifications);
        
        if (switchNotifs != null && prefs != null) {
            boolean isNotifEnabled = prefs.getBoolean("notifications_enabled", true);
            switchNotifs.setChecked(isNotifEnabled);
            
            switchNotifs.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Notifications " + (isChecked ? "activées" : "désactivées"), Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        if (btnNotifSettings != null && switchNotifs != null) {
            btnNotifSettings.setOnClickListener(v -> {
                switchNotifs.setChecked(!switchNotifs.isChecked());
            });
        }

        View.OnClickListener settingsListener = v -> {
            if (getActivity() != null) {
                startActivity(new Intent(requireActivity(), SettingsActivity.class));
            }
        };
        View btnSet = view.findViewById(R.id.btnSettings);
        if (btnSet != null) btnSet.setOnClickListener(settingsListener);
        
        View btnSetTop = view.findViewById(R.id.btnSettingsTop);
        if (btnSetTop != null) btnSetTop.setOnClickListener(settingsListener);

        View btnHelp = view.findViewById(R.id.btnHelp);
        if (btnHelp != null) btnHelp.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(requireActivity(), HelpActivity.class));
            }
        });

        View btnLogout = view.findViewById(R.id.menuLogout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> {
            if (prefs != null) prefs.edit().clear().apply();
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Déconnecté", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    // Refresh name when returning from EditProfileActivity
    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) loadUserInfo(getView());
    }
}