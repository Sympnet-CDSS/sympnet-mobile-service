package com.sympnet.app.activities.doctor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sympnet.app.R;
import com.sympnet.app.adapters.DoctorAdapter;
import com.sympnet.app.model.Doctor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoriteDoctorsActivity extends AppCompatActivity {

    private static final String PREFS_FAVORITES = "doctor_favorites";
    private static final String PREFS_DOCTORS   = "doctors_cache";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_doctors);

        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView recycler  = findViewById(R.id.recyclerFavorites);
        LinearLayout tvEmpty = findViewById(R.id.tvEmpty);


        recycler.setLayoutManager(new LinearLayoutManager(this));

        List<Doctor> favDoctors = loadFavoriteDoctors();

        if (favDoctors.isEmpty()) {
            recycler.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            recycler.setAdapter(new DoctorAdapter(favDoctors));
        }
    }

   
    private List<Doctor> loadFavoriteDoctors() {
        List<Doctor> result = new ArrayList<>();

        //  Récupère les IDs favoris 
        SharedPreferences appPrefs = getSharedPreferences("SympNetPrefs", Context.MODE_PRIVATE);
        String userId = appPrefs.getString("userId", "default_user");
        String prefix = "fav_" + userId + "_";

        SharedPreferences favPrefs = getSharedPreferences(PREFS_FAVORITES, Context.MODE_PRIVATE);
        Map<String, ?> all = favPrefs.getAll();

        List<String> favIds = new ArrayList<>();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().startsWith(prefix)
                    && Boolean.TRUE.equals(entry.getValue())) {
                favIds.add(entry.getKey().replace(prefix, ""));
            }
        }

        if (favIds.isEmpty()) return result;

        // Récupère le cache des médecins
        SharedPreferences doctorPrefs = getSharedPreferences(PREFS_DOCTORS, Context.MODE_PRIVATE);
        String json = doctorPrefs.getString("all_doctors", null);
        if (json == null) return result;

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = String.valueOf(obj.getInt("id"));
                if (favIds.contains(id)) {
                    Doctor d = new Doctor();
                    d.setId(obj.getInt("id"));
                    d.setFirstName(obj.optString("firstName", ""));
                    d.setLastName(obj.optString("lastName", ""));
                    d.setSpeciality(obj.optString("speciality", ""));
                    d.setRating((float) obj.optDouble("rating", 0.0));
                    d.setLatitude(obj.optDouble("latitude", 0.0));
                    d.setLongitude(obj.optDouble("longitude", 0.0));
                    d.setAddress(obj.optString("address", ""));
                    result.add(d);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
