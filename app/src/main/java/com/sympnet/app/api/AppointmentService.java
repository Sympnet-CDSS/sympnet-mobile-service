package com.sympnet.app.api;

import com.sympnet.app.model.AppointmentCreatedResponse;
import com.sympnet.app.model.AppointmentDto;
import com.sympnet.app.model.CreateAppointmentRequest;
import com.sympnet.app.model.PatientNotificationDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

public interface AppointmentService {

    @POST("api/appointments")
    Call<AppointmentCreatedResponse> createAppointment(
            @Header("Authorization") String token,
            @Body CreateAppointmentRequest request);


    @GET("api/appointments/doctor/{doctorId}/booked-slots")
    Call<List<String>> getDoctorBookedSlots(
            @Header("Authorization") String token,
            @Path("doctorId") int doctorId);

    @GET("api/appointments")
    Call<List<AppointmentDto>> getMyAppointments(
            @Header("Authorization") String token);

    @GET("api/appointments/{id}")
    Call<AppointmentDto> getAppointmentById(
            @Header("Authorization") String token,
            @Path("id") int id);

    @DELETE("api/appointments/{id}")
    Call<Void> cancelAppointment(
            @Header("Authorization") String token,
            @Path("id") int id);
    // Ajouter dans AppointmentService.java
    @GET("api/patient-notifications")
    Call<List<PatientNotificationDto>> getMyNotifications(
            @Header("Authorization") String token
    );

    @PATCH("api/patient-notifications/read-all")
    Call<Void> markAllRead(
            @Header("Authorization") String token
    );
}