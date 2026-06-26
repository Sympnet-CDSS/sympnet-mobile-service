package com.sympnet.app.model;

import com.google.gson.annotations.SerializedName;

public class CreateAppointmentRequest {

    @SerializedName("doctorId")           public int     doctorId;

    // On envoie un DateTime 
    @SerializedName("dateTime")           public String  dateTime;

    @SerializedName("duration")           public int     duration = 30;
    @SerializedName("type")               public String  type;        
    @SerializedName("isUrgent")           public boolean isUrgent;
    @SerializedName("notes")              public String  notes;
    @SerializedName("reason")             public String  reason;

}