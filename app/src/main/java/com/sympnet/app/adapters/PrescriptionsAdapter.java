package com.sympnet.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sympnet.app.R;
import com.sympnet.app.model.PrescriptionDto;

import java.util.List;

public class PrescriptionsAdapter extends RecyclerView.Adapter<PrescriptionsAdapter.ViewHolder> {

    private List<PrescriptionDto> list;
    private Context context;

    public PrescriptionsAdapter(Context context, List<PrescriptionDto> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prescription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrescriptionDto dto = list.get(position);
        holder.tvName.setText(dto.getTitle());
        holder.tvDate.setText(dto.getDate());
        holder.tvDoctor.setText(dto.getDoctorName());

        holder.itemView.setOnClickListener(v -> {
            if (dto.getPdfUrl() != null && !dto.getPdfUrl().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dto.getPdfUrl()));
                context.startActivity(browserIntent);
            } else {
                Toast.makeText(context, "Le fichier PDF n'est pas encore disponible.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvDoctor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPrescriptionName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDoctor = itemView.findViewById(R.id.tvDoctorName);
        }
    }
}
