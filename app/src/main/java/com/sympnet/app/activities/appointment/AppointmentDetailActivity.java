package com.sympnet.app.activities.appointment;
import com.sympnet.app.activities.BaseActivity;
import com.sympnet.app.activities.chat.ChatDetailActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.app.R;
import com.sympnet.app.home.ActivityHome;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;
import com.sympnet.app.api.AppointmentService;
import com.sympnet.app.model.AppointmentDto;
import com.sympnet.app.model.Conversation;
import com.sympnet.app.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppointmentDetailActivity extends BaseActivity {

    //  Views 
    private TextView  tvDoctorName, tvDoctorSpecialty, tvLocation;
    private TextView  tvAppointmentDate, tvAppointmentTime;
    private TextView  tvAppointmentType, tvUrgent;
    private TextView  tvStatus, tvReason, tvNotes;
    private RatingBar ratingBar;
    private EditText  etReviewComment;
    private View      btnSubmitReview;
    private Button    btnDone;
    private ImageView btnBack;
    private View      btnMessage, btnCancel;

    private static final DateTimeFormatter DATE_DISPLAY =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_DISPLAY =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        bindViews();
        setupBottomNav();

        btnBack.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> {
            startActivity(new Intent(this, ActivityHome.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });

        // Placeholder for cancel action
        btnCancel.setOnClickListener(v -> {
            Toast.makeText(this, "Annulation...", Toast.LENGTH_SHORT).show();
        });

        int appointmentId = getIntent().getIntExtra("appointmentId", -1);
        if (appointmentId != -1) loadAppointment(appointmentId);
    }

    // ── Bind
    private void bindViews() {
        tvDoctorName      = findViewById(R.id.tvDoctorName);//pour lier les vues XML au code Java
        tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty);
        tvLocation        = findViewById(R.id.tvLocation);
        tvAppointmentDate = findViewById(R.id.tvAppointmentDate);
        tvAppointmentTime = findViewById(R.id.tvAppointmentTime);
        tvAppointmentType = findViewById(R.id.tvAppointmentType);
        tvUrgent          = findViewById(R.id.tvUrgent);
        tvStatus          = findViewById(R.id.tvStatus);
        tvReason          = findViewById(R.id.tvReason);
        tvNotes           = findViewById(R.id.tvNotes);
        ratingBar         = findViewById(R.id.ratingBar);
        etReviewComment   = findViewById(R.id.etReviewComment);
        btnSubmitReview   = findViewById(R.id.btnSubmitReview);
        btnDone           = findViewById(R.id.btnDone);
        btnBack           = findViewById(R.id.btnBack);
        btnMessage        = findViewById(R.id.btnMessage);
        btnCancel         = findViewById(R.id.btnCancel);
    }

    //  API 
    private void loadAppointment(int id) {
        SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("userToken", "");

        AppointmentService service = ApiClient.getClient().create(AppointmentService.class);
        service.getAppointmentById(token, id).enqueue(new Callback<AppointmentDto>() {
            @Override
            public void onResponse(Call<AppointmentDto> call, Response<AppointmentDto> resp) {
                if (resp.isSuccessful() && resp.body() != null) populate(resp.body());
            }
            @Override
            public void onFailure(Call<AppointmentDto> call, Throwable t) {
                Toast.makeText(AppointmentDetailActivity.this,
                        "Chargement échoué", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //  Populate ─
    private void populate(AppointmentDto a) {
        tvDoctorName.setText(a.doctorName != null ? a.doctorName : "—");
        tvDoctorSpecialty.setText(a.doctorSpeciality != null ? a.doctorSpeciality : "—");
        
        if (tvLocation != null) {
            tvLocation.setText(a.doctorAddress != null ? a.doctorAddress : "Adresse non spécifiée");
        }

        LocalDate date = LocalDate.parse(a.getAppointmentDate());
        LocalTime time = LocalTime.parse(a.getAppointmentTime());
        tvAppointmentDate.setText(date.format(DATE_DISPLAY));
        tvAppointmentTime.setText(time.format(TIME_DISPLAY));

        boolean inPerson = "InPerson".equalsIgnoreCase(a.type);
        tvAppointmentType.setText(inPerson ? "Consultation au cabinet" : "Téléconsultation");

        tvUrgent.setVisibility(a.isUrgent ? View.VISIBLE : View.GONE);

        tvStatus.setText(a.status != null ? a.status : "—");
        tvStatus.setTextColor(statusColor(a.status));

        tvReason.setText(a.reason != null && !a.reason.isEmpty() ? a.reason : "—");
        tvNotes.setText(a.notes  != null && !a.notes.isEmpty()  ? a.notes  : "—");

        // Conditionally display the message button if status is confirmed
        if (a.status != null && (a.status.equalsIgnoreCase("Confirmed") || a.status.equalsIgnoreCase("Confirmé"))) {
            btnMessage.setVisibility(View.VISIBLE);
            
            // Set real chat logic here
            btnMessage.setOnClickListener(v -> openChat(a));
        } else {
            btnMessage.setVisibility(View.GONE);
        }

        // --- REAL RATING AND REVIEW SYSTEM ---
        String doctorKey = String.valueOf(a.doctorId);
        SharedPreferences ratingsPrefs = getSharedPreferences("doctor_ratings", MODE_PRIVATE);
        float savedRating = ratingsPrefs.getFloat(doctorKey, 0f);
        String savedComment = ratingsPrefs.getString(doctorKey + "_comment", "");

        if (savedRating > 0f) {
            ratingBar.setRating(savedRating);
        }
        if (!savedComment.isEmpty()) {
            etReviewComment.setText(savedComment);
            if (btnSubmitReview instanceof TextView) {
                ((TextView) btnSubmitReview).setText("Modifier mon avis");
            }
        }

        btnSubmitReview.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etReviewComment.getText().toString().trim();

            if (rating == 0f) {
                Toast.makeText(this, "Veuillez sélectionner une note.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save locally so that it persists and updates
            ratingsPrefs.edit()
                    .putFloat(doctorKey, rating)
                    .putString(doctorKey + "_comment", comment)
                    .apply();

            Toast.makeText(this, "Merci ! Votre avis a été publié avec succès. ✅", Toast.LENGTH_SHORT).show();
            if (btnSubmitReview instanceof TextView) {
                ((TextView) btnSubmitReview).setText("Modifier mon avis");
            }
        });
    }
    
    private void openChat(AppointmentDto a) {
        String token     = "Bearer " + SessionManager.getInstance(this).getUserToken();
        String patientId = SessionManager.getInstance(this).getCurrentUserId();

        Map<String, String> body = new HashMap<>();
        body.put("doctorId",  String.valueOf(a.doctorId));
        body.put("patientId", patientId);

        Toast.makeText(this, "Ouverture de la messagerie...", Toast.LENGTH_SHORT).show();

        ApiClient.getClient().create(ApiService.class)
                .createConversation(token, body)
                .enqueue(new Callback<Conversation>() {
                    @Override
                    public void onResponse(Call<Conversation> call, Response<Conversation> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Conversation conv = response.body();
                            conv.setOtherUserName(a.doctorName);
                            conv.setOtherUserRole(a.doctorSpeciality);
                            ChatDetailActivity.start(AppointmentDetailActivity.this, conv);
                        } else {
                            Toast.makeText(AppointmentDetailActivity.this,
                                    "Erreur " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Conversation> call, Throwable t) {
                        Toast.makeText(AppointmentDetailActivity.this,
                                "Erreur connexion", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //  Helpers 
    private int statusColor(String status) {
        if (status == null) return getColor(R.color.status_pending);
        return switch (status) {
            case "Confirmed" -> getColor(R.color.status_confirmed);
            case "Confirmé"  -> getColor(R.color.status_confirmed);
            case "Cancelled" -> getColor(R.color.status_cancelled);
            case "Completed" -> getColor(R.color.status_completed);
            default          -> getColor(R.color.status_pending);
        };
    }
}