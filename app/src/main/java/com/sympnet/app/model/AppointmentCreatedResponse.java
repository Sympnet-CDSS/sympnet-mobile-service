
package com.sympnet.app.model;

import com.google.gson.annotations.SerializedName;

public class AppointmentCreatedResponse {
    @SerializedName("message")       public String message;
    @SerializedName("appointmentId") public int    appointmentId;
}