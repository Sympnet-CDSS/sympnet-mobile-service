package com.sympnet.mobile.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import com.sympnet.mobile.R;

public class HomeFragment extends Fragment {

    private Button btnBody3D, btnFindDoctor, btnConsultation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnBody3D = view.findViewById(R.id.btn_body_3d);
        btnFindDoctor = view.findViewById(R.id.btn_find_doctor);
        btnConsultation = view.findViewById(R.id.btn_consultation);

        btnBody3D.setOnClickListener(v -> {});
        btnFindDoctor.setOnClickListener(v -> {});
        btnConsultation.setOnClickListener(v -> {});

        return view;
    }
}