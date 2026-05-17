package com.sympnet.app.activities;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sympnet.app.R;
import com.sympnet.app.adapters.DoctorAdapter;
import com.sympnet.app.model.Doctor;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllDoctorsActivity extends BaseActivity {

    private static final String TAG = "AllDoctorsActivity";

    private RecyclerView recyclerDoctors;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> allDoctors = new ArrayList<>();
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_doctors);

        initViews();
        setupBackButton();
        setupRecyclerView();
        setupSearch();
        loadDoctors();
    }

    private void initViews() {
        recyclerDoctors = findViewById(R.id.recyclerAllDoctors);
        etSearch        = findViewById(R.id.etSearchAll);
    }

    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btnBackAllDoctors);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        recyclerDoctors.setLayoutManager(new LinearLayoutManager(this));
        recyclerDoctors.setHasFixedSize(false);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors(s.toString());
            }
        });
    }

    private void filterDoctors(String query) {
        if (doctorAdapter == null) return;

        if (query.trim().isEmpty()) {
            doctorAdapter.updateList(allDoctors);
            return;
        }

        String lower = query.toLowerCase().trim();
        List<Doctor> filtered = new ArrayList<>();
        for (Doctor d : allDoctors) {
            if ((d.getName() != null && d.getName().toLowerCase().contains(lower))
                    || (d.getSpecialty() != null && d.getSpecialty().toLowerCase().contains(lower))) {
                filtered.add(d);
            }
        }
        doctorAdapter.updateList(filtered);
    }

    private void loadDoctors() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getDoctors().enqueue(new Callback<List<Doctor>>() {
            @Override
            public void onResponse(Call<List<Doctor>> call, Response<List<Doctor>> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && !response.body().isEmpty()) {

                    allDoctors = response.body();
                    doctorAdapter = new DoctorAdapter(allDoctors);
                    recyclerDoctors.setAdapter(doctorAdapter);
                    Log.d(TAG, "Loaded " + allDoctors.size() + " doctors");

                } else {
                    Log.w(TAG, "Empty doctor list. HTTP " + response.code());
                    Toast.makeText(AllDoctorsActivity.this,
                            "Aucun médecin disponible", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Doctor>> call, Throwable t) {
                Log.e(TAG, "loadDoctors failed", t);
                Toast.makeText(AllDoctorsActivity.this,
                        "Erreur de connexion: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}