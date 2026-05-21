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
            String titleLowerForFilter = n.title != null ? n.title.toLowerCase() : "";
            if (titleLowerForFilter.contains("message") || titleLowerForFilter.contains("nouveau message")) {
                continue;
            }

            View item = LayoutInflater.from(this)
                    .inflate(R.layout.item_notification_detail, notifContainer, false);

            androidx.cardview.widget.CardView cardContainer = item.findViewById(R.id.cardContainer);
            View iconContainer = item.findViewById(R.id.iconContainer);
            ImageView ivIcon = item.findViewById(R.id.ivIcon);
            TextView tvTitle   = item.findViewById(R.id.tvNotifTitle);
            TextView tvMessage = item.findViewById(R.id.tvNotifMessage);
            TextView tvTimeAgo = item.findViewById(R.id.tvNotifTimeAgo);
            TextView tvExactDate = item.findViewById(R.id.tvNotifExactDate);
            View dot = item.findViewById(R.id.unreadDot);

            String titleLower = n.title != null ? n.title.toLowerCase() : "";

            if (titleLower.contains("ordonnance")) {
                cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF5F5"));
                iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEBEB")));
                ivIcon.setImageResource(R.drawable.ic_pill);
                tvExactDate.setVisibility(View.GONE);
            } else if (titleLower.contains("confirm")) {
                cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
                iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E6FFED")));
                ivIcon.setImageResource(R.drawable.ic_check_green);
                tvExactDate.setVisibility(View.VISIBLE);
                if (n.sentAt != null && n.sentAt.length() >= 16) {
                    tvExactDate.setText("📅 " + formatShortDate(n.sentAt));
                }
            } else {
                cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#F9FAFB"));
                iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E5E7EB")));
                ivIcon.setImageResource(R.drawable.ic_bell_gray);
                tvExactDate.setVisibility(View.GONE);
            }

            String cleanTitle = n.title != null ? n.title.replace("💊", "").replace("✅", "").replace("📅", "").trim() : "";
            String cleanMessage = n.message != null ? n.message.replace("💊", "").replace("✅", "").replace("📅", "").trim() : "";

            if (tvTitle   != null) tvTitle.setText(cleanTitle);
            if (tvMessage != null) tvMessage.setText(cleanMessage);
            if (tvTimeAgo != null) tvTimeAgo.setText("📅 " + getTimeAgo(n.sentAt));
            if (dot != null) dot.setVisibility(n.isRead ? View.GONE : View.VISIBLE);

            item.setOnClickListener(v -> {
                if (titleLower.contains("ordonnance")) {
                    startActivity(new android.content.Intent(NotificationDetailsActivity.this, com.sympnet.app.activities.prescription.PrescriptionsActivity.class));
                    return;
                }
                
                String target = "HOME";
                if (titleLower.contains("message") || titleLower.contains("chat")) {
                    target = "CHAT";
                } else if (titleLower.contains("rendez-vous") || titleLower.contains("appointment") || titleLower.contains("rappel")) {
                    target = "SCHEDULE";
                }
                if (!target.equals("HOME")) {
                    android.content.Intent intent = new android.content.Intent(NotificationDetailsActivity.this, com.sympnet.app.activities.MainActivity.class);
                    intent.putExtra("TARGET_FRAGMENT", target);
                    startActivity(intent);
                }
            });

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

    private String getTimeAgo(String dateString) {
        if (dateString == null) return "l'instant";
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date date = sdf.parse(dateString);
            if (date == null) return dateString;
            long diff = System.currentTimeMillis() - date.getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            long hours = diff / (1000 * 60 * 60);
            long mins = diff / (1000 * 60);
            if (days > 0) return "Il y a " + days + "j";
            if (hours > 0) return "Il y a " + hours + "h";
            if (mins > 0) return "Il y a " + mins + "m";
            return "À l'instant";
        } catch (Exception e) {
            return dateString;
        }
    }

    private String formatShortDate(String dateString) {
        if (dateString == null) return "";
        try {
            java.text.SimpleDateFormat sdfIn = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            sdfIn.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date date = sdfIn.parse(dateString);
            if (date == null) return dateString;
            
            java.text.SimpleDateFormat sdfOut = new java.text.SimpleDateFormat("dd MMM à HH:mm", java.util.Locale.getDefault());
            return sdfOut.format(date);
        } catch (Exception e) {
            return dateString.substring(0, 16).replace("T", " ");
        }
    }
}