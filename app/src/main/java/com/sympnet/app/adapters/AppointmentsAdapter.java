package com.sympnet.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sympnet.app.R;
import com.sympnet.app.activities.AppointmentDetailActivity;
import com.sympnet.app.activities.BookAppointmentActivity;
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
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

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
        holder.tvDoctorName.setText(a.doctorName != null ? a.doctorName : "—");

        // Date + Time
        try {
            if (a.dateTime != null && a.dateTime.length() >= 19) {
                LocalDateTime dt = LocalDateTime.parse(
                        a.dateTime.substring(0, 19), INPUT_FMT);
                holder.tvDateTime.setText(
                        "📅 " + dt.toLocalDate().format(DATE_FMT) +
                                "  🕐 " + dt.toLocalTime().format(TIME_FMT));
            }
        } catch (Exception e) {
            holder.tvDateTime.setText(a.dateTime != null ? a.dateTime : "—");
        }

        // Status
        holder.tvStatus.setText(a.status != null ? a.status : "—");
        if (a.status != null) {
            switch (a.status) {
                case "Confirmed":
                    holder.tvStatus.setTextColor(0xFF4CAF50);
                    holder.tvStatus.getBackground().setTint(0xFFE8F5E9);
                    break;
                case "Cancelled":
                case "Annulé":
                    holder.tvStatus.setTextColor(0xFFD32F2F);
                    holder.tvStatus.getBackground().setTint(0xFFFFEBEE);
                    break;
                case "Completed":
                    holder.tvStatus.setTextColor(0xFF1976D2);
                    holder.tvStatus.getBackground().setTint(0xFFE3F2FD);
                    break;
                default: // En attente
                    holder.tvStatus.setTextColor(0xFFFF9800);
                    holder.tvStatus.getBackground().setTint(0xFFFFF3E0);
                    break;
            }
        }

        // Type
        if (a.type != null) {
            boolean inPerson = "InPerson".equalsIgnoreCase(a.type);
            holder.tvType.setText(inPerson ? "🏥 In-Person" : "📹 Teleconsult");
        }

        // Urgent
        holder.tvUrgent.setVisibility(a.isUrgent ? View.VISIBLE : View.GONE);

        // Reason
        if (a.reason != null && !a.reason.isEmpty()) {
            holder.tvReason.setVisibility(View.VISIBLE);
            holder.tvReason.setText("📋 " + a.reason);
        } else {
            holder.tvReason.setVisibility(View.GONE);
        }

        // Masquer Cancel et Reschedule si annulé ou complété
        boolean canAct = !"Annulé".equals(a.status)
                && !"Cancelled".equals(a.status)
                && !"Completed".equals(a.status);
        holder.btnCancel.setVisibility(canAct ? View.VISIBLE : View.GONE);
        holder.btnReschedule.setVisibility(canAct ? View.VISIBLE : View.GONE);

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
            intent.putExtra("rescheduleId", a.id);
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return appointments != null ? appointments.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvDateTime, tvStatus, tvType, tvUrgent, tvReason;
        Button btnCancel, btnReschedule, btnDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName  = itemView.findViewById(R.id.tvDoctorName);
            tvDateTime    = itemView.findViewById(R.id.tvDateTime);
            tvStatus      = itemView.findViewById(R.id.tvStatus);
            tvType        = itemView.findViewById(R.id.tvType);
            tvUrgent      = itemView.findViewById(R.id.tvUrgent);
            tvReason      = itemView.findViewById(R.id.tvReason);
            btnCancel     = itemView.findViewById(R.id.btnCancel);
            btnReschedule = itemView.findViewById(R.id.btnReschedule);
            btnDetails    = itemView.findViewById(R.id.btnDetails);
        }
    }
}