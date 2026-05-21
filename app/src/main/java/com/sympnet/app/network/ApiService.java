package com.sympnet.app.network;

import com.sympnet.app.api.AppointmentService;
import com.sympnet.app.model.AudioRequest;
import com.sympnet.app.model.Conversation;
import com.sympnet.app.model.Doctor;
import com.sympnet.app.model.Message;
import com.sympnet.app.model.Patient;
import com.sympnet.app.model.PatientNotificationDto;
import com.sympnet.app.model.TranscriptionResult;
import com.sympnet.app.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ── Auth
    @POST("api/auth/login")
    Call<User> login(@Body Map<String, String> credentials);

    @POST("api/auth/register-mobile")
    Call<User> register(@Body Map<String, String> body);

    @POST("api/auth/google-mobile")
    Call<User> googleLogin(@Body Map<String, String> body);

    @POST("api/auth/forgot-password")
    Call<Void> forgotPassword(@Body Map<String, String> body);

    @POST("api/auth/verify-reset-code")
    Call<Void> verifyResetCode(@Body Map<String, String> body);

    @POST("api/auth/reset-password")
    Call<Void> resetPassword(@Body Map<String, String> body);

    @POST("api/auth/verify-code")
    Call<Void> verifyCode(@Body Map<String, String> body);

    // ── Patient
    @GET("api/patients/{id}")
    Call<Patient> getPatientByUserId(
            @Header("Authorization") String bearerToken,
            @Path("id") String userId);

    @PUT("api/patients/{id}")
    Call<Void> updatePatient(
            @Header("Authorization") String bearerToken,
            @Path("id") String userId,
            @Body Map<String, Object> body);

    @retrofit2.http.DELETE("api/patients/{id}")
    Call<Void> deletePatientAccount(
            @Header("Authorization") String bearerToken,
            @Path("id") String userId);

    // ── Doctors
    @GET("api/doctors")
    Call<List<Doctor>> getDoctors();

    @GET("api/workinghours/doctor/{doctorId}")
    Call<List<Map<String, Object>>> getDoctorWorkingHours(
            @Path("doctorId") String doctorId);

    // ── Speech
    @POST("api/Speech/transcribe")
    Call<TranscriptionResult> transcribeAudio(@Body AudioRequest request);

    // ── Chat
    @GET("api/chat/conversations")
    Call<List<Conversation>> getConversations(
            @Header("Authorization") String bearerToken);

    @POST("api/chat/conversations")
    Call<Conversation> createConversation(
            @Header("Authorization") String bearerToken,
            @Body Map<String, String> body);


    @GET("api/chat/conversations/{conversationId}/messages")
    Call<List<Message>> getConversationMessages(
            @Header("Authorization") String bearerToken,
            @Path("conversationId") String conversationId);

    @POST("api/chat/messages")
    Call<Message> sendMessage(
            @Header("Authorization") String bearerToken,
            @Body Map<String, Object> body);

    @POST("api/chat/conversations/{id}/read")
    Call<Void> markConversationAsRead(
            @Header("Authorization") String bearerToken,
            @Path("id") String conversationId);

    // ── Notifications patient
    @GET("api/patient-notifications")
    Call<List<PatientNotificationDto>> getMyNotifications(
            @Header("Authorization") String bearerToken);

    @PATCH("api/patient-notifications/read-all")
    Call<Void> markAllRead(
            @Header("Authorization") String bearerToken);

    @GET("api/appointments/confirmed")
    Call<List<Object>> getConfirmedAppointments(
            @Header("Authorization") String bearerToken,
            @Query("patientId") String patientId,
            @Query("doctorId") String doctorId
    );

    // ── Ordonnances
    @GET("api/ordonnances/my")
    Call<List<com.sympnet.app.model.PrescriptionDto>> getMyOrdonnances(
            @Header("Authorization") String bearerToken
    );

    // ── AI
    @POST("api/ai/diagnostic")
    Call<java.util.Map<String, Object>> getDiagnostic(
            @Header("Authorization") String bearerToken,
            @Body java.util.Map<String, Object> body);

    @POST("api/ai/symptom-to-doctor")
    Call<java.util.Map<String, Object>> symptomToDoctor(
            @Header("Authorization") String bearerToken,
            @Body java.util.Map<String, Object> body);
}
