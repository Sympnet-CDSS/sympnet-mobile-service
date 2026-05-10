package com.sympnet.app.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView filterAll, filterPending, filterConfirmed, filterCancelled;
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

        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        setupFilters();
        loadAppointments();

        return view;
    }

    private void loadAppointments() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("userToken", "");

        AppointmentService service = ApiClient.getClient()
                .create(AppointmentService.class);

        service.getMyAppointments(token).enqueue(new Callback<List<AppointmentDto>>() {
            @Override
            public void onResponse(Call<List<AppointmentDto>> call,
                                   Response<List<AppointmentDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allAppointments = response.body();
                    showAppointments(allAppointments);
                } else {
                    Log.e("APPOINTMENTS", "Error: " + response.code());
                    Toast.makeText(getContext(),
                            "Erreur " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentDto>> call, Throwable t) {
                Log.e("APPOINTMENTS", "onFailure: " + t.getMessage());
                Toast.makeText(getContext(),
                        "Connexion échouée", Toast.LENGTH_SHORT).show();
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
            List<AppointmentDto> filtered = new ArrayList<>();
            for (AppointmentDto a : allAppointments) {
                if ("En attente".equalsIgnoreCase(a.status)) filtered.add(a);
            }
            showAppointments(filtered);
        });

        filterConfirmed.setOnClickListener(v -> {
            setActiveFilter(filterConfirmed);
            List<AppointmentDto> filtered = new ArrayList<>();
            for (AppointmentDto a : allAppointments) {
                if ("Confirmed".equalsIgnoreCase(a.status)) filtered.add(a);
            }
            showAppointments(filtered);
        });

        filterCancelled.setOnClickListener(v -> {
            setActiveFilter(filterCancelled);
            List<AppointmentDto> filtered = new ArrayList<>();
            for (AppointmentDto a : allAppointments) {
                if ("Annulé".equalsIgnoreCase(a.status) ||
                        "Cancelled".equalsIgnoreCase(a.status)) {
                    filtered.add(a);
                }
            }
            showAppointments(filtered);
        });
    }

    private void setActiveFilter(TextView selected) {
        for (TextView filter : new TextView[]{
                filterAll, filterPending, filterConfirmed, filterCancelled}) {
            filter.setBackgroundTintList(
                    requireContext().getColorStateList(R.color.gray_100));
            filter.setTextColor(requireContext().getColor(R.color.gray_600));
        }
        selected.setBackgroundTintList(
                requireContext().getColorStateList(R.color.teal_500));
        selected.setTextColor(requireContext().getColor(android.R.color.white));
    }
}