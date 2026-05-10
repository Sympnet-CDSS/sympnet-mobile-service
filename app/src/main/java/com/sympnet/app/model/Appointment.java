package com.sympnet.app.model;

public class Appointment {

    public int id;

    // --- Parties liées ---
    public String patientId;         // Guid → String en Java
    public int doctorId;

    // --- Date / Heure ---
    public String appointmentDate;   // "yyyy-MM-dd"
    public String appointmentTime;   // "HH:mm"
    public int duration;

    // --- Type & Statut ---
    public String status;            // "Pending", "Confirmed", "Cancelled", "Completed"
    public String type;              // "InPerson", "Teleconsultation"

    // --- Booking pour quelqu'un d'autre ---
    public boolean isForOther;
    public String otherPatientName;
    public Integer otherPatientAge;
    public String otherPatientGender;

    // --- Détails médicaux ---
    public String reason;
    public String notes;
    public boolean isUrgent;

    // --- Timestamps ---
    public String createdAt;
    public String updatedAt;
    public String confirmedAt;
    public String cancelledAt;
}