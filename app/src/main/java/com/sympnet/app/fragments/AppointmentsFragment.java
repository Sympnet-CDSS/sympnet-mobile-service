package com.sympnet.app.fragments;
import com.sympnet.app.activities.notification.NotificationDetailsActivity;
import com.sympnet.app.activities.doctor.AllDoctorsActivity;

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
    private TextView filterAll, filterPending, filterConfirmed, filterCompleted;
    private TextView tvCountPending, tvCountConfirmed, tvCountTotal;
    private List<AppointmentDto> allAppointments = new ArrayList<>();
    private AppointmentsAdapter adapter;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAddAppointment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        rvAppointments  = view.findViewById(R.id.rvAppointments);
        filterAll       = view.findViewById(R.id.filterAll);
        filterPending   = view.findViewById(R.id.filterPending);
        filterConfirmed = view.findViewById(R.id.filterConfirmed);
        filterCompleted = view.findViewById(R.id.filterCompleted);

        tvCountPending   = view.findViewById(R.id.tvCountPending);
        tvCountConfirmed = view.findViewById(R.id.tvCountConfirmed);
        tvCountTotal     = view.findViewById(R.id.tvCountTotal);
        fabAddAppointment = view.findViewById(R.id.fabAddAppointment);

        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        View btnNotifications = view.findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(getContext(), com.sympnet.app.activities.notification.NotificationDetailsActivity.class);
                startActivity(intent);
            });
        }

        setupFilters();
        setupFab();
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
        updateStatistics();
    }

    private void updateStatistics() {
        int pending = 0, confirmed = 0;
        for (AppointmentDto a : allAppointments) {
            String s = a.status != null ? a.status : "";
            if (s.isEmpty() || s.equalsIgnoreCase("Pending") || s.equalsIgnoreCase("En attente")) {
                pending++;
            } else if (s.equalsIgnoreCase("Confirmed") || s.equalsIgnoreCase("Confirmé")) {
                confirmed++;
            }
        }
        tvCountPending.setText(String.valueOf(pending));
        tvCountConfirmed.setText(String.valueOf(confirmed));
        tvCountTotal.setText(String.valueOf(allAppointments.size()));
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
            } else if ("Completed".equals(targetStatus)) {
                if (s.equalsIgnoreCase("Completed") || s.equalsIgnoreCase("Terminé")) {
                    filtered.add(a);
                }
            }
        }
        showAppointments(filtered);
    }

    private void setupFab() {
        if (fabAddAppointment != null) {
            fabAddAppointment.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(getContext(), com.sympnet.app.activities.doctor.AllDoctorsActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setActiveFilter(TextView selected) {
        TextView[] filters = {filterAll, filterPending, filterConfirmed, filterCompleted};
        for (TextView f : filters) {
            f.setBackgroundResource(0);
            f.setTextColor(Color.parseColor("#333333"));
        }
        selected.setBackgroundResource(R.drawable.bg_btn_teal);
        selected.setTextColor(Color.WHITE);
    }
}
