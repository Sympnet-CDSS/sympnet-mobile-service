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

    /**
     * Fills the header with real data from SharedPreferences.
     * Keys written by LoginActivity.saveAuthData() + savePatientData():
     *   "userName"   → firstName + lastName (or email fallback)
     *   "userEmail"  → email from AuthResponseDto
     */
    private void loadUserInfo(View view) {
        TextView tvName    = view.findViewById(R.id.tvUserName);
        TextView tvEmail   = view.findViewById(R.id.tvUserEmail);
        ImageView ivAvatar = view.findViewById(R.id.ivProfileAvatar);

        String name     = prefs.getString("userName",       "");
        String email    = prefs.getString("userEmail",      "");
        String base64   = prefs.getString("userPhotoBase64", null);

        tvName.setText(name.isEmpty()  ? "Patient" : name);
        tvEmail.setText(email.isEmpty() ? "" : email);

        if (base64 != null) {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bmp   = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp != null) {
                Glide.with(this)
                        .load(bmp)
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.ic_profile_avatar)
                        .into(ivAvatar);
            }
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_profile_avatar)
                    .transform(new CircleCrop())
                    .into(ivAvatar);
        }
    }

    private void setupMenuClicks(View view) {
        // Edit Profile — opens EditProfileActivity pre-filled with all patient data
        view.findViewById(R.id.menuProfile).setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), EditProfileActivity.class)));

        // Favorite Doctors
        view.findViewById(R.id.menuFavorite).setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), FavoriteDoctorsActivity.class)));

        // Payment
        view.findViewById(R.id.menuPayment).setOnClickListener(v ->
                Toast.makeText(getActivity(), "Payment coming soon", Toast.LENGTH_SHORT).show());

        // Privacy Policy
        view.findViewById(R.id.menuPrivacy).setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), PrivacyPolicyActivity.class)));

        // Settings
        view.findViewById(R.id.menuSettings).setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), SettingsActivity.class)));

        // Help
        view.findViewById(R.id.menuHelp).setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), HelpActivity.class)));

        // Logout — clears all session data
        view.findViewById(R.id.menuLogout).setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // Refresh name when returning from EditProfileActivity
    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) loadUserInfo(getView());
    }
}