package com.sympnet.app.activities.prescription;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sympnet.app.R;
import com.sympnet.app.adapters.PrescriptionsAdapter;
import com.sympnet.app.model.PrescriptionDto;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionsActivity extends AppCompatActivity {

    private RecyclerView rvPrescriptions;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescriptions);

        rvPrescriptions = findViewById(R.id.rvPrescriptions);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        rvPrescriptions.setLayoutManager(new LinearLayoutManager(this));
        
        SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("userToken", "");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getMyOrdonnances(token).enqueue(new Callback<List<PrescriptionDto>>() {
            @Override
            public void onResponse(Call<List<PrescriptionDto>> call, Response<List<PrescriptionDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PrescriptionDto> ordonnances = response.body();
                    if (ordonnances.isEmpty()) {
                        Toast.makeText(PrescriptionsActivity.this, "Vous n'avez aucune ordonnance.", Toast.LENGTH_SHORT).show();
                    }
                    PrescriptionsAdapter adapter = new PrescriptionsAdapter(PrescriptionsActivity.this, ordonnances);
                    rvPrescriptions.setAdapter(adapter);
                } else {
                    Toast.makeText(PrescriptionsActivity.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    Log.e("Prescriptions", "Erreur: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PrescriptionDto>> call, Throwable t) {
                Toast.makeText(PrescriptionsActivity.this, "Problème de connexion", Toast.LENGTH_SHORT).show();
                Log.e("Prescriptions", "Erreur réseau: " + t.getMessage());
            }
        });
    }
}
