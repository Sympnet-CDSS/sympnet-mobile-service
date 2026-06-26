package com.sympnet.app.model;

import com.google.gson.annotations.SerializedName;

public class AppointmentDto {
    public int id;

    // Doctor
    @SerializedName("doctorId")     public int    doctorId;
    @SerializedName("doctorName")   public String doctorName;
    @SerializedName("doctorSpeciality") public String doctorSpeciality;
    @SerializedName("doctorAddress")    public String doctorAddress;

    @SerializedName("dateTime")     public String dateTime;   // "yyyy-MM-ddTHH:mm:ss"

    @SerializedName("duration")         public int     duration   = 30;
    @SerializedName("type")             public String  type       = "InPerson";
    @SerializedName("isUrgent")         public boolean isUrgent   = false;
    @SerializedName("status")           public String  status;
    @SerializedName("notes")            public String  notes;
    @SerializedName("reason")           public String  reason;


    // Helpers pour splitter date and time
    public String getAppointmentDate() {
        if (dateTime == null || dateTime.length() < 10) return "";
        return dateTime.substring(0, 10); 
    }

    public String getAppointmentTime() {
        if (dateTime == null || dateTime.length() < 16) return "00:00";
        return dateTime.substring(11, 16); // "HH:mm"
    }
}