package com.sympnet.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.sympnet.app.R;
import com.sympnet.app.home.ActivityHome;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private Button btnBody3D, btnFindDoctor, btnConsultation;
    private TextView tvWelcome;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvWelcome = view.findViewById(R.id.tv_welcome_title);
        btnBody3D = view.findViewById(R.id.btn_body_3d);
        btnFindDoctor = view.findViewById(R.id.btn_find_doctor);
        btnConsultation = view.findViewById(R.id.btn_consultation);

        // REAL DATA: Get User Name from SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences("SympNetPrefs", Context.MODE_PRIVATE);
        String name = prefs.getString("userName", "User");
        
        // REAL DATA: Get Current Date
        String currentDate = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(new Date());

        tvWelcome.setText("Welcome back, " + name + "!\n" + currentDate);

        // NAVIGATION
        btnFindDoctor.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ActivityHome.class);
            startActivity(intent);
        });

        btnBody3D.setOnClickListener(v -> {
            // Intent for Body 3D if activity exists
        });

        btnConsultation.setOnClickListener(v -> {
            // Intent for Consultation if activity exists
        });
    }
}
