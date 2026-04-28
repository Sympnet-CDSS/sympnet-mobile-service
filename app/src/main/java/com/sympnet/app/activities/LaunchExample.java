package com.sympnet.app.activities;

// ─────────────────────────────────────────────────────────────────────────────
// HOW TO LAUNCH DoctorDetailsActivity
//
// Call this from your doctor-list adapter click handler, search result, etc.
// All doctor data is passed via Intent extras — DoctorDetailsActivity reads
// them in loadIntentData() and populates the UI automatically.
// ─────────────────────────────────────────────────────────────────────────────

import android.content.Intent;

import com.sympnet.app.model.Doctor;

public class LaunchExample {

    /**
     * Example — call from an Adapter's onBindViewHolder or a Fragment's click handler.
     *
     * @param context   any Context (Activity, Fragment.requireContext(), etc.)
     * @param doctor    your Doctor model object (or individual fields)
     */
    public static void openDoctorDetails(android.content.Context context, Doctor doctor) {

        Intent intent = new Intent(context, DoctorDetailsActivity.class);

        // ── Required extras ──────────────────────────────────────────────────
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ID,        doctor.getId());
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_NAME,      doctor.getFullName());
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_SPECIALTY, doctor.getSpecialty());
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_RATING,    doctor.getRating());  // float

        // ── Location extras (used by the embedded map) ───────────────────────
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LAT,       doctor.getLatitude());   // double
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LNG,       doctor.getLongitude());  // double

        context.startActivity(intent);
    }

    // ── Or inline if you don't have a Doctor model ───────────────────────────
    public static void openDoctorDetailsRaw(android.content.Context context) {

        Intent intent = new Intent(context, DoctorDetailsActivity.class);
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ID,        "doc_042");
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_NAME,      "Dr. Olivia Turner, M.D.");
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_SPECIALTY, "Dermato-Endocrinology");
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_RATING,    4.5f);
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LAT,       36.8189);  // Tunis example
        intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LNG,       10.1658);
        context.startActivity(intent);
    }
}