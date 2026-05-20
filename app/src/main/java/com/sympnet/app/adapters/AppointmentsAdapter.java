package com.sympnet.app.adapters;
import com.sympnet.app.activities.appointment.AppointmentDetailActivity;
import com.sympnet.app.activities.appointment.BookAppointmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sympnet.app.R;
import com.sympnet.app.api.AppointmentService;
import com.sympnet.app.model.AppointmentDto;
import com.sympnet.app.network.ApiClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.ViewHolder> {

    private List<AppointmentDto> appointments;

    private static final DateTimeFormatter INPUT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm", Locale.FRENCH);

    public AppointmentsAdapter(List<AppointmentDto> appointments) {
        this.appointments = appointments;
    }

    public void updateList(List<AppointmentDto> newList) {
        this.appointments = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentDto a = appointments.get(position);
        Context ctx = holder.itemView.getContext();

        // Doctor name
        holder.tvDoctorName.setText(a.doctorName != null ? a.doctorName : "Médecin");

        // Initials
        if (a.doctorName != null && !a.doctorName.isEmpty()) {
            String[] parts = a.doctorName.split(" ");
            String initials = "";
            if (parts.length > 0) initials += parts[0].charAt(0);
            if (parts.length > 1) initials += parts[1].charAt(0);
            holder.tvInitials.setText(initials.toUpperCase());
        }

        // Doctor specialty 
        holder.tvDoctorSpecialty.setText("Généraliste"); 

        // Date + Time
        try {
            if (a.dateTime != null && a.dateTime.length() >= 16) {
                String cleanDt = a.dateTime;
                if (cleanDt.length() > 19) cleanDt = cleanDt.substring(0, 19);
                else if (cleanDt.length() == 16) cleanDt += ":00";
                
                LocalDateTime dt = LocalDateTime.parse(cleanDt, INPUT_FMT);
                holder.tvDate.setText(dt.format(DATE_FMT));
                holder.tvTime.setText(dt.format(TIME_FMT));
            } else {
                holder.tvDate.setText(a.dateTime != null ? a.dateTime : "—");
                holder.tvTime.setText("--:--");
            }
        } catch (Exception e) {
            holder.tvDate.setText(a.dateTime != null ? a.dateTime : "—");
            holder.tvTime.setText("--:--");
        }

        // Status
        applyStatus(holder, a.status);

        // Type
        if (a.type != null) {
            boolean inPerson = "InPerson".equalsIgnoreCase(a.type);
            holder.tvType.setText(inPerson ? "Cabinet" : "Téléconsultation");
            if (inPerson) {
                holder.ivTypeIcon.setImageResource(R.drawable.ic_home);
                holder.ivTypeIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#009688")));
                holder.tvType.setTextColor(Color.parseColor("#009688"));
                holder.layoutType.setBackgroundResource(R.drawable.bg_label_light_teal);
            } else {
                holder.ivTypeIcon.setImageResource(R.drawable.ic_videocam);
                holder.ivTypeIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#9C27B0")));
                holder.tvType.setTextColor(Color.parseColor("#9C27B0"));
                holder.layoutType.setBackgroundResource(R.drawable.bg_label_purple);
            }
        }

        // Reason
        if (a.reason != null && !a.reason.isEmpty()) {
            holder.tvReason.setText(a.reason);
        } else {
            holder.tvReason.setText("Consulation de routine");
        }

        // Action buttons visibility
        boolean isFinal = "Annulé".equals(a.status) || "Cancelled".equals(a.status) || 
                         "Completed".equals(a.status) || "Terminé".equals(a.status);
        holder.btnCancel.setVisibility(isFinal ? View.GONE : View.VISIBLE);
        holder.btnReschedule.setVisibility(isFinal ? View.GONE : View.VISIBLE);

        // Details
        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(ctx, AppointmentDetailActivity.class);
            intent.putExtra("appointmentId", a.id);
            ctx.startActivity(intent);
        });

        // Cancel
        holder.btnCancel.setOnClickListener(v -> {
            SharedPreferences prefs = ctx.getSharedPreferences("SympNetPrefs",
                    Context.MODE_PRIVATE);
            String token = "Bearer " + prefs.getString("userToken", "");

            AppointmentService service = ApiClient.getClient()
                    .create(AppointmentService.class);

            service.cancelAppointment(token, a.id).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        a.status = "Annulé";
                        notifyItemChanged(holder.getAdapterPosition());
                        Toast.makeText(ctx, "Rendez-vous annulé", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ctx, "Erreur " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ctx, "Connexion échouée", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Reschedule
        holder.btnReschedule.setOnClickListener(v -> {
            Intent intent = new Intent(ctx, BookAppointmentActivity.class);
            intent.putExtra("doctorId", a.doctorId);
            intent.putExtra("doctorName", a.doctorName);
            intent.putExtra("doctorSpecialty", a.doctorSpeciality != null ? a.doctorSpeciality : "Généraliste");
            intent.putExtra("doctorAddress", a.doctorAddress != null ? a.doctorAddress : "Cabinet médical");
            intent.putExtra("rescheduleId", a.id);
            ctx.startActivity(intent);
        });
    }

    private void applyStatus(ViewHolder h, String status) {
        if (status == null) status = "";
        switch (status) {
            case "Confirmé":
            case "Confirmed":
                h.tvStatus.setText("Confirmé");
                h.tvStatus.setTextColor(Color.parseColor("#059669"));
                h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D1FAE5")));
                break;
            case "Annulé":
            case "Cancelled":
                h.tvStatus.setText("Annulé");
                h.tvStatus.setTextColor(Color.parseColor("#DC2626"));
                h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                break;
            case "Terminé":
            case "Completed":
                h.tvStatus.setText("Terminé");
                h.tvStatus.setTextColor(Color.parseColor("#6B7280"));
                h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E5E7EB")));
                break;
            default:
                h.tvStatus.setText("En attente");
                h.tvStatus.setTextColor(Color.parseColor("#D97706"));
                h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return appointments != null ? appointments.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvDoctorSpecialty, tvDate, tvTime, tvStatus, tvType, tvReason, tvInitials;
        TextView btnCancel, btnReschedule, btnDetails;
        View layoutType;
        ImageView ivTypeIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName      = itemView.findViewById(R.id.tvDoctorName);
            tvDoctorSpecialty = itemView.findViewById(R.id.tvDoctorSpecialty);
            tvDate            = itemView.findViewById(R.id.tvDate);
            tvTime            = itemView.findViewById(R.id.tvTime);
            tvStatus          = itemView.findViewById(R.id.tvStatus);
            tvType            = itemView.findViewById(R.id.tvType);
            tvReason          = itemView.findViewById(R.id.tvReason);
            tvInitials        = itemView.findViewById(R.id.tvInitials);
            btnCancel         = itemView.findViewById(R.id.btnCancel);
            btnReschedule     = itemView.findViewById(R.id.btnReschedule);
            btnDetails        = itemView.findViewById(R.id.btnDetails);
            layoutType        = itemView.findViewById(R.id.layoutType);
            ivTypeIcon        = itemView.findViewById(R.id.ivTypeIcon);
        }
    }
}
