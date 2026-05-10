package com.sympnet.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.sympnet.app.R;
import com.sympnet.app.model.AppointmentCreatedResponse;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.api.AppointmentService;
import com.sympnet.app.model.AppointmentType;
import com.sympnet.app.model.CreateAppointmentRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class BookAppointmentActivity extends BaseActivity {

    // ── Views ──────────────────────────────────────────────────
    private LinearLayout btnTypeInPerson, btnTypeTeleconsult;
    private LinearLayout calendarStrip;
    private GridLayout   timeSlotsGrid;
    private SwitchCompat switchUrgent;
    private EditText     etReason, etNotes;
    private Button       btnConfirmBooking;
    private ImageView    btnBack;

    // ── State ──────────────────────────────────────────────────
    private int             doctorId;
    private AppointmentType selectedType = AppointmentType.InPerson;
    private LocalDate       selectedDate = LocalDate.now();
    private LocalTime       selectedTime = null;


    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        doctorId = getIntent().getIntExtra("doctorId", -1);

        bindViews();
        setupBottomNav();
        setupTypeToggle();
        setupCalendarStrip();
        setupTimeSlots();
        setupConfirmButton();

        btnBack.setOnClickListener(v -> finish());
    }

    // ── Bind ───────────────────────────────────────────────────
    private void bindViews() {
        btnTypeInPerson    = findViewById(R.id.btnTypeInPerson);
        btnTypeTeleconsult = findViewById(R.id.btnTypeTeleconsult);
        calendarStrip      = findViewById(R.id.calendarStrip);
        timeSlotsGrid      = findViewById(R.id.timeSlotsGrid);
        switchUrgent       = findViewById(R.id.switchUrgent);
        etReason           = findViewById(R.id.etReason);
        etNotes            = findViewById(R.id.etNotes);
        btnConfirmBooking  = findViewById(R.id.btnConfirmBooking);
        btnBack            = findViewById(R.id.btnBack);
    }

    // ── Appointment Type ───────────────────────────────────────
    private void setupTypeToggle() {
        btnTypeInPerson.setOnClickListener(v -> selectType(AppointmentType.InPerson));
        btnTypeTeleconsult.setOnClickListener(v -> selectType(AppointmentType.Teleconsultation));
        applyTypeUI(AppointmentType.InPerson);
    }

    private void selectType(AppointmentType type) {
        selectedType = type;
        applyTypeUI(type);
    }

    private void applyTypeUI(AppointmentType type) {
        boolean inPerson = type == AppointmentType.InPerson;
        btnTypeInPerson.setBackgroundTintList(
                getColorStateList(inPerson ? R.color.teal_500 : R.color.gray_100));
        btnTypeTeleconsult.setBackgroundTintList(
                getColorStateList(inPerson ? R.color.gray_100 : R.color.teal_500));
    }

    // ── Calendar Strip ─────────────────────────────────────────
    // ── Calendar Strip ─────────────────────────────────────────
    private void setupCalendarStrip() {
        calendarStrip.removeAllViews();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 14; i++) {
            LocalDate date = today.plusDays(i);
            boolean isSelected = date.equals(selectedDate);

            TextView dayView = new TextView(this);
            dayView.setText(date.getDayOfWeek().name().substring(0, 3) + "\n"
                    + date.getDayOfMonth());
            dayView.setGravity(android.view.Gravity.CENTER);
            dayView.setPadding(24, 16, 24, 16);
            dayView.setTextSize(12);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(8);
            dayView.setLayoutParams(params);

            if (isSelected) {
                dayView.setBackgroundResource(R.drawable.search_bg);
                dayView.getBackground().setTint(getColor(R.color.teal_500));
                dayView.setTextColor(getColor(android.R.color.white));
                dayView.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                dayView.setBackgroundResource(R.drawable.search_bg);
                dayView.getBackground().setTint(getColor(R.color.gray_100));
                dayView.setTextColor(getColor(R.color.gray_600));
            }

            LocalDate finalDate = date;
            dayView.setOnClickListener(v -> {
                selectedDate = finalDate;
                setupCalendarStrip();
            });

            calendarStrip.addView(dayView);
        }
    }

    // ── Time Slots ─────────────────────────────────────────────
    private final String[] TIME_SLOTS = {
            "09:00","09:30","10:00","10:30",
            "11:00","11:30","12:00","12:30",
            "13:00","13:30","14:00","14:30",
            "15:00","15:30","16:00"
    };

    private void setupTimeSlots() {
        timeSlotsGrid.removeAllViews();
        for (String slot : TIME_SLOTS) {
            LocalTime t = LocalTime.parse(slot);
            boolean isSelected = t.equals(selectedTime);

            TextView tv = new TextView(this);
            tv.setText(slot);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setPadding(8, 16, 8, 16);
            tv.setTextSize(12);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(4, 4, 4, 4);
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            tv.setLayoutParams(params);

            if (isSelected) {
                tv.setBackgroundResource(R.drawable.search_bg);
                tv.getBackground().setTint(getColor(R.color.teal_500));
                tv.setTextColor(getColor(android.R.color.white));
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tv.setBackgroundResource(R.drawable.search_bg);
                tv.getBackground().setTint(0xFFF5F5F5);
                tv.setTextColor(0xFF333333);
            }

            tv.setOnClickListener(v -> {
                selectedTime = t;
                setupTimeSlots();
            });

            timeSlotsGrid.addView(tv);
        }
    }

    // ── Confirm ────────────────────────────────────────────────
    private void setupConfirmButton() {
        btnConfirmBooking.setOnClickListener(v -> submitAppointment());
    }

    private void submitAppointment() {
        if (selectedTime == null) {
            Toast.makeText(this, "Veuillez sélectionner un horaire", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        String token = prefs.getString("userToken", "");
        android.util.Log.d("BOOKING", "token = [" + token + "]");
        android.util.Log.d("BOOKING", "doctorId = " + doctorId);

        if (token.isEmpty()) {
            Toast.makeText(this, "Session expirée, reconnectez-vous", Toast.LENGTH_LONG).show();
            return;
        }

        if (doctorId == -1) {
            Toast.makeText(this, "Erreur : doctorId manquant", Toast.LENGTH_LONG).show();
            return;
        }

        String combinedDateTime = selectedDate.toString() + "T" + selectedTime.format(TIME_FMT) + ":00";
        android.util.Log.d("BOOKING", "dateTime = " + combinedDateTime);

        CreateAppointmentRequest req = new CreateAppointmentRequest();
        req.doctorId = doctorId;
        req.dateTime = combinedDateTime;
        req.type     = selectedType.name();
        req.isUrgent = switchUrgent.isChecked();
        req.reason   = etReason.getText().toString().trim();
        req.notes    = etNotes.getText().toString().trim();

        btnConfirmBooking.setEnabled(false);
        btnConfirmBooking.setText("Envoi…");

        AppointmentService service = ApiClient.getClient().create(AppointmentService.class);
        // ← passer "Bearer " + token comme premier argument
        service.createAppointment("Bearer " + token, req).enqueue(new Callback<AppointmentCreatedResponse>() {
            @Override
            public void onResponse(Call<AppointmentCreatedResponse> call,
                                   Response<AppointmentCreatedResponse> response) {
                btnConfirmBooking.setEnabled(true);
                btnConfirmBooking.setText("Confirm Appointment");

                android.util.Log.d("BOOKING", "response code = " + response.code());

                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody() != null
                                ? response.errorBody().string() : "null";
                        android.util.Log.e("BOOKING", "error body = " + errorBody);
                        Toast.makeText(BookAppointmentActivity.this,
                                "Erreur " + response.code() + " : " + errorBody,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        android.util.Log.e("BOOKING", "could not read error body", e);
                    }
                    return;
                }

                if (response.body() != null) {
                    int newId = response.body().appointmentId;
                    android.util.Log.d("BOOKING", "appointmentId = " + newId);
                    Toast.makeText(BookAppointmentActivity.this,
                            "Rendez-vous créé ✓", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(BookAppointmentActivity.this,
                            AppointmentDetailActivity.class);
                    intent.putExtra("appointmentId", newId);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<AppointmentCreatedResponse> call, Throwable t) {
                btnConfirmBooking.setEnabled(true);
                btnConfirmBooking.setText("Confirm Appointment");
                android.util.Log.e("BOOKING", "onFailure: " + t.getMessage(), t);
                Toast.makeText(BookAppointmentActivity.this,
                        "Connexion échouée : " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}