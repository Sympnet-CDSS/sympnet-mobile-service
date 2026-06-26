package com.sympnet.app.activities;
import com.sympnet.app.activities.doctor.DoctorDetailsActivity;

import android.content.Intent;

import com.sympnet.app.model.Doctor;

public class LaunchExample {

    /**
     * @param context   
     * @param doctor    
     */
    public static void openDoctorDetails(android.content.Context context, Doctor doctor) {

        Intent intent = new Intent(context, DoctorDetailsActivity.class);

        // Required extras
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ID,        doctor.getId());
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_NAME,      doctor.getFullName());
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_SPECIALTY, doctor.getSpecialty());
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_RATING,    doctor.getRating());  

        // Location extras 
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LAT,       doctor.getLatitude());   
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LNG,       doctor.getLongitude()); 

        context.startActivity(intent);
    }


    public static void openDoctorDetailsRaw(android.content.Context context) {

        Intent intent = new Intent(context, DoctorDetailsActivity.class);
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ID,        "doc_042");
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_NAME,      "Dr. Olivia Turner, M.D.");
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_SPECIALTY, "Dermato-Endocrinology");
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_RATING,    4.5f);
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LAT,       36.8189);
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LNG,       10.1658);
        context.startActivity(intent);
    }
}