package com.sympnet.app.network;

import com.sympnet.app.model.AudioRequest;
import com.sympnet.app.model.Doctor;
import com.sympnet.app.model.Patient;
import com.sympnet.app.model.TranscriptionResult;
import com.sympnet.app.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    //Auth

    @POST("api/auth/login")
    Call<User> login(@Body Map<String, String> credentials);

    @POST("api/auth/register-mobile")
    Call<User> register(@Body Map<String, String> body);

    //Password reset 

    //Password reset 

    @POST("api/auth/forgot-password")
    Call<Void> forgotPassword(@Body Map<String, String> body);

    @POST("api/auth/verify-reset-code")
    Call<Void> verifyResetCode(@Body Map<String, String> body);

    @POST("api/auth/reset-password")
    Call<Void> resetPassword(@Body Map<String, String> body);
    /** Step 2 — POST /api/auth/verify-code  { "email": "...", "code": "..." } */
    @POST("api/auth/verify-code")
    Call<Void> verifyCode(@Body Map<String, String> body);


    //Patient 

    /**
     * GET /api/patients/{id}
     * {id} = UserId (Guid) from AuthResponseDto.
     * Requires Bearer token.
     */
    @GET("api/patients/{id}")
    Call<Patient> getPatientByUserId(
            @Header("Authorization") String bearerToken,
            @Path("id") String userId
    );

    //Doctors 

    @GET("api/doctors")
    Call<List<Doctor>> getDoctors();

    //Speech Transcription

    @POST("api/Speech/transcribe")
    Call<TranscriptionResult> transcribeAudio(
            @Header("Authorization") String bearerToken,
            @Body AudioRequest request
    );
}