package com.sympnet.app.fragments;

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
import com.sympnet.app.activities.EditProfileActivity;
import com.sympnet.app.activities.FavoriteDoctorsActivity;
import com.sympnet.app.activities.HelpActivity;
import com.sympnet.app.activities.LoginActivity;
import com.sympnet.app.activities.PrivacyPolicyActivity;
import com.sympnet.app.activities.SettingsActivity;
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

        String name     = prefs.getString("userName",       "");
        String email    = prefs.getString("userEmail",      "");
        String phone    = prefs.getString("userPhone",      "+216 20 123 456");
        String address  = prefs.getString("userAddress",    "Tunis, Tunisie");
        String birthday = prefs.getString("userBirthday",   "15 Mars 1995");
        String base64   = prefs.getString("userPhotoBase64", null);

        tvName.setText(name.isEmpty()  ? "Patient" : name);
        tvEmail.setText(email.isEmpty() ? "" : email);
        
        if (tvInfoEmail != null) tvInfoEmail.setText(email);
        if (tvInfoPhone != null) tvInfoPhone.setText(phone);
        if (tvInfoAddress != null) tvInfoAddress.setText(address);
        if (tvInfoBirthday != null) tvInfoBirthday.setText(birthday);

        if (base64 != null) {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bmp   = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp != null) {
                Glide.with(this)
                        .load(bmp)
                        .placeholder(R.drawable.ic_profile_avatar)
                        .into(ivAvatar);
            }
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_profile_avatar)
                    .into(ivAvatar);
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

        // Stats: Rendez-vous
        TextView tvCountApp = view.findViewById(R.id.tvCountApp);
        String token = "Bearer " + prefs.getString("userToken", "");
        com.sympnet.app.network.ApiClient.getClient().create(com.sympnet.app.api.AppointmentService.class)
                .getMyAppointments(token).enqueue(new retrofit2.Callback<java.util.List<com.sympnet.app.model.AppointmentDto>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<java.util.List<com.sympnet.app.model.AppointmentDto>> call,
                                   @NonNull retrofit2.Response<java.util.List<com.sympnet.app.model.AppointmentDto>> response) {
                if (response.isSuccessful() && response.body() != null && tvCountApp != null) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> tvCountApp.setText(String.valueOf(response.body().size())));
                    }
                }
            }
            @Override
            public void onFailure(@NonNull retrofit2.Call<java.util.List<com.sympnet.app.model.AppointmentDto>> call, @NonNull Throwable t) {
                if (tvCountApp != null) tvCountApp.setText("0");
            }
        });
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