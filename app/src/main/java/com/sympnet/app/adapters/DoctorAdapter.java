package com.sympnet.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sympnet.app.R;
import com.sympnet.app.activities.DoctorDetailsActivity;
import com.sympnet.app.model.Doctor;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    private List<Doctor> doctors;
    private static final String PREFS_FAVORITES = "doctor_favorites";

    public DoctorAdapter(List<Doctor> doctors) {
        this.doctors = doctors;
    }

    public void updateList(List<Doctor> newList) {
        this.doctors = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        Context context = holder.itemView.getContext();

        holder.name.setText("Dr. " + doctor.getName());
        holder.specialty.setText(doctor.getSpecialty());

        // ── Helper pour créer l'intent avec toutes les infos ──────────────
        // (évite la répétition)

        // ── Bouton Info → ouvre DoctorDetailsActivity ────────────────────
        holder.btnInfo.setOnClickListener(v -> {
            Intent intent = new Intent(context, DoctorDetailsActivity.class);
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ID,        String.valueOf(doctor.getId()));
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_NAME,      "Dr. " + doctor.getName());
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_SPECIALTY, doctor.getSpecialty());
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_RATING,    doctor.getRating());
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LAT,       doctor.getLatitude());
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LNG,       doctor.getLongitude());
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ADDRESS,   doctor.getAddress());
            context.startActivity(intent);
        });

        // ── Bouton Calendrier → ouvre DoctorDetailsActivity section RDV ──
        holder.btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(context, DoctorDetailsActivity.class);
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ID,        String.valueOf(doctor.getId()));
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_NAME,      "Dr. " + doctor.getName());
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_SPECIALTY, doctor.getSpecialty());
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_RATING,    doctor.getRating());
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LAT,       doctor.getLatitude());
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LNG,       doctor.getLongitude());
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ADDRESS,   doctor.getAddress());
            intent.putExtra("SCROLL_TO_CALENDAR", true);
            context.startActivity(intent);
        });

        // ── Bouton ? → affiche les infos du médecin ───────────────────────
        holder.btnQuestion.setOnClickListener(v -> {
            String info = "👨‍⚕️ Dr. " + doctor.getName() + "\n"
                    + "🏥 Spécialité : " + doctor.getSpecialty() + "\n"
                    + "📍 Adresse : " + (doctor.getAddress() != null ? doctor.getAddress() : "N/A");

            new android.app.AlertDialog.Builder(context)
                    .setTitle("Informations")
                    .setMessage(info)
                    .setPositiveButton("OK", null)
                    .show();
        });

        // ── Bouton Favori → ajoute/retire des favoris ─────────────────────
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FAVORITES, Context.MODE_PRIVATE);
        String key = "fav_" + doctor.getId();
        boolean isFav = prefs.getBoolean(key, false);
        holder.btnFavorite.setText(isFav ? "♥" : "♡");
        holder.btnFavorite.setTextColor(isFav
                ? context.getResources().getColor(android.R.color.holo_red_light)
                : 0xFF0D6E6A);

        holder.btnFavorite.setOnClickListener(v -> {
            boolean currentFav = prefs.getBoolean(key, false);
            prefs.edit().putBoolean(key, !currentFav).apply();
            holder.btnFavorite.setText(!currentFav ? "♥" : "♡");
            holder.btnFavorite.setTextColor(!currentFav
                    ? context.getResources().getColor(android.R.color.holo_red_light)
                    : 0xFF0D6E6A);
            Toast.makeText(context,
                    !currentFav ? "Ajouté aux favoris" : "Retiré des favoris",
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() { return doctors.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, specialty, btnInfo, btnCalendar, btnQuestion, btnFavorite;

        ViewHolder(View itemView) {
            super(itemView);
            name        = itemView.findViewById(R.id.docName);
            specialty   = itemView.findViewById(R.id.docSpecialty);
            btnInfo     = itemView.findViewById(R.id.btnInfo);
            btnCalendar = itemView.findViewById(R.id.btnCalendar);
            btnQuestion = itemView.findViewById(R.id.btnQuestion);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}