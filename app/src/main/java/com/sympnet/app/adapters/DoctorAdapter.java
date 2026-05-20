package com.sympnet.app.adapters;
import com.sympnet.app.activities.chat.ChatDetailActivity;
import com.sympnet.app.activities.doctor.DoctorDetailsActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sympnet.app.R;
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

        // Initials
        if (doctor.getName() != null && !doctor.getName().trim().isEmpty()) {
            String[] parts = doctor.getName().trim().split("\\s+");
            String initials = "";
            if (parts.length > 0 && !parts[0].isEmpty()) initials += parts[0].charAt(0);
            if (parts.length > 1 && !parts[1].isEmpty()) initials += parts[1].charAt(0);
            holder.initials.setText(initials.toUpperCase());
        } else {
            holder.initials.setText("D");
        }

        // Rating
        holder.rating.setText(String.valueOf(doctor.getRating()));

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

        // Message button removed

        // ── Bouton Info (Circle) → affiche les infos du médecin ────────────
        holder.btnInfoCircle.setOnClickListener(v -> {
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
        
        holder.btnFavorite.setImageResource(isFav 
                ? android.R.drawable.btn_star_big_on 
                : android.R.drawable.btn_star_big_off);
        holder.btnFavorite.setColorFilter(android.graphics.Color.parseColor(isFav ? "#FFC107" : "#1A2A3A"));

        holder.btnFavorite.setOnClickListener(v -> {
            boolean currentFav = prefs.getBoolean(key, false);
            boolean newFav = !currentFav;
            prefs.edit().putBoolean(key, newFav).apply();
            
            holder.btnFavorite.setImageResource(newFav 
                    ? android.R.drawable.btn_star_big_on 
                    : android.R.drawable.btn_star_big_off);
            holder.btnFavorite.setColorFilter(android.graphics.Color.parseColor(newFav ? "#FFC107" : "#1A2A3A"));
            
            Toast.makeText(context,
                    newFav ? "Ajouté aux favoris" : "Retiré des favoris",
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() { return doctors.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, specialty, btnInfo, initials, rating;
        ImageView btnCalendar, btnInfoCircle, btnFavorite;

        ViewHolder(View itemView) {
            super(itemView);
            name          = itemView.findViewById(R.id.docName);
            specialty     = itemView.findViewById(R.id.docSpecialty);
            btnInfo       = itemView.findViewById(R.id.btnInfo);
            initials      = itemView.findViewById(R.id.tvDocInitials);
            rating        = itemView.findViewById(R.id.tvDocRating);
            btnCalendar   = itemView.findViewById(R.id.btnCalendar);
            btnInfoCircle = itemView.findViewById(R.id.btnInfoCircle);
            btnFavorite   = itemView.findViewById(R.id.btnFavorite);
        }
    }
}