package com.sympnet.app.fragments;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sympnet.app.R;
import com.sympnet.app.adapters.AppointmentsAdapter;
import com.sympnet.app.api.AppointmentService;
import com.sympnet.app.model.AppointmentDto;
import com.sympnet.app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class AppointmentsFragment extends Fragment {

    private RecyclerView rvAppointments;
    private TextView filterAll, filterPending, filterConfirmed, filterCancelled, filterCompleted;
    private List<AppointmentDto> allAppointments = new ArrayList<>();
    private AppointmentsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        rvAppointments  = view.findViewById(R.id.rvAppointments);
        filterAll       = view.findViewById(R.id.filterAll);
        filterPending   = view.findViewById(R.id.filterPending);
        filterConfirmed = view.findViewById(R.id.filterConfirmed);
        filterCancelled = view.findViewById(R.id.filterCancelled);
        filterCompleted = view.findViewById(R.id.filterCompleted);

        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        setupFilters();
        loadAppointments();

        return view;
    }

    private void loadAppointments() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("userToken", "");

        ApiClient.getClient().create(AppointmentService.class)
                .getMyAppointments(token).enqueue(new Callback<List<AppointmentDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<AppointmentDto>> call,
                                   @NonNull Response<List<AppointmentDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allAppointments = response.body();
                    showAppointments(allAppointments);
                } else {
                    Log.e("APPOINTMENTS", "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AppointmentDto>> call, @NonNull Throwable t) {
                Log.e("APPOINTMENTS", "onFailure: " + t.getMessage());
                Toast.makeText(getContext(), "Connexion échouée", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAppointments(List<AppointmentDto> list) {
        adapter = new AppointmentsAdapter(list);
        rvAppointments.setAdapter(adapter);
    }

    private void setupFilters() {
        filterAll.setOnClickListener(v -> {
            setActiveFilter(filterAll);
            showAppointments(allAppointments);
        });

        filterPending.setOnClickListener(v -> {
            setActiveFilter(filterPending);
            filterByStatus("Pending");
        });

        filterConfirmed.setOnClickListener(v -> {
            setActiveFilter(filterConfirmed);
            filterByStatus("Confirmed");
        });

        filterCompleted.setOnClickListener(v -> {
            setActiveFilter(filterCompleted);
            filterByStatus("Completed");
        });

        filterCancelled.setOnClickListener(v -> {
            setActiveFilter(filterCancelled);
            filterByStatus("Cancelled");
        });
    }

    private void filterByStatus(String targetStatus) {
        List<AppointmentDto> filtered = new ArrayList<>();
        for (AppointmentDto a : allAppointments) {
            String s = a.status != null ? a.status : "";
            if ("Pending".equals(targetStatus)) {
                if (s.isEmpty() || s.equalsIgnoreCase("Pending") || s.equalsIgnoreCase("En attente")) {
                    filtered.add(a);
                }
            } else if ("Confirmed".equals(targetStatus)) {
                if (s.equalsIgnoreCase("Confirmed") || s.equalsIgnoreCase("Confirmé")) {
                    filtered.add(a);
                }
            } else if ("Cancelled".equals(targetStatus)) {
                if (s.equalsIgnoreCase("Cancelled") || s.equalsIgnoreCase("Annulé")) {
                    filtered.add(a);
                }
            } else if ("Completed".equals(targetStatus)) {
                if (s.equalsIgnoreCase("Completed") || s.equalsIgnoreCase("Terminé")) {
                    filtered.add(a);
                }
            }
        }
        showAppointments(filtered);
    }

    private void setActiveFilter(TextView selected) {
        TextView[] filters = {filterAll, filterPending, filterConfirmed, filterCancelled, filterCompleted};
        for (TextView f : filters) {
            f.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
            f.setTextColor(Color.parseColor("#9E9E9E"));
        }
        selected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#009688")));
        selected.setTextColor(Color.WHITE);
    }
}
