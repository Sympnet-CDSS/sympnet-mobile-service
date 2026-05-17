package com.sympnet.app.activities.notification;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sympnet.app.R;
import com.sympnet.app.api.AppointmentService;
import com.sympnet.app.model.PatientNotificationDto;
import com.sympnet.app.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationDetailsActivity extends AppCompatActivity {

    private LinearLayout notifContainer;
    private TextView     tvMarkAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        notifContainer = findViewById(R.id.notifContainer);
        tvMarkAll      = findViewById(R.id.tvMarkAll);

        loadNotifications();

        tvMarkAll.setOnClickListener(v -> markAllRead());
    }

    private void loadNotifications() {
        SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("userToken", "");

        AppointmentService service = ApiClient.getClient()
                .create(AppointmentService.class);

        service.getMyNotifications(token).enqueue(
                new Callback<List<PatientNotificationDto>>() {
                    @Override
                    public void onResponse(Call<List<PatientNotificationDto>> call,
                                           Response<List<PatientNotificationDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            displayNotifications(response.body());
                        } else {
                            Log.e("NOTIF", "Erreur: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PatientNotificationDto>> call,
                                          Throwable t) {
                        Log.e("NOTIF", "onFailure: " + t.getMessage());
                    }
                });
    }

    private void displayNotifications(List<PatientNotificationDto> notifs) {
        notifContainer.removeAllViews();

        if (notifs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aucune notification");
            empty.setTextColor(0xFF9E9E9E);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setPadding(0, 80, 0, 0);
            notifContainer.addView(empty);
            return;
        }

        for (PatientNotificationDto n : notifs) {
            View item = LayoutInflater.from(this)
                    .inflate(R.layout.item_notification_detail, notifContainer, false);

            TextView tvTitle   = item.findViewById(R.id.tvNotifTitle);
            TextView tvMessage = item.findViewById(R.id.tvNotifMessage);
            TextView tvTime    = item.findViewById(R.id.tvNotifTime);
            View     dot       = item.findViewById(R.id.unreadDot);

            if (tvTitle   != null) tvTitle.setText(n.title);
            if (tvMessage != null) tvMessage.setText(n.message);
            if (tvTime    != null && n.sentAt != null && n.sentAt.length() >= 16)
                tvTime.setText(n.sentAt.substring(0, 16).replace("T", " "));
            if (dot != null)
                dot.setVisibility(n.isRead ? View.GONE : View.VISIBLE);

            notifContainer.addView(item);
        }
    }

    private void markAllRead() {
        SharedPreferences prefs = getSharedPreferences("SympNetPrefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("userToken", "");

        AppointmentService service = ApiClient.getClient()
                .create(AppointmentService.class);

        service.markAllRead(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(NotificationDetailsActivity.this,
                        "Tout marqué comme lu ✓", Toast.LENGTH_SHORT).show();
                loadNotifications();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("NOTIF", t.getMessage());
            }
        });
    }
}