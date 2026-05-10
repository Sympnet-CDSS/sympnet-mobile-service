package com.sympnet.app.model;

import com.google.gson.annotations.SerializedName;

public class CreateAppointmentRequest {

    @SerializedName("doctorId")           public int     doctorId;

    // On envoie un DateTime combiné au backend : "yyyy-MM-ddTHH:mm:ss"
    @SerializedName("dateTime")           public String  dateTime;

    @SerializedName("duration")           public int     duration = 30;
    @SerializedName("type")               public String  type;          // "InPerson" | "Teleconsultation"
    @SerializedName("isUrgent")           public boolean isUrgent;
    @SerializedName("notes")              public String  notes;
    @SerializedName("reason")             public String  reason;

}