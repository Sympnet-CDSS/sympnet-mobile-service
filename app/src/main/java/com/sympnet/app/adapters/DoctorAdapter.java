package com.sympnet.app.adapters;


import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sympnet.app.R;
import com.sympnet.app.model.Doctor;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    private List<Doctor> doctors;

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
        holder.name.setText(doctor.getName());
        holder.specialty.setText(doctor.getSpecialty());
    }

    @Override
    public int getItemCount() { return doctors.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, specialty;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.docName);
            specialty = itemView.findViewById(R.id.docSpecialty);
        }
    }
}