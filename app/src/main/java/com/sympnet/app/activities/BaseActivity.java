package com.sympnet.app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.app.R;
import com.sympnet.app.home.ActivityHome;
import com.sympnet.app.model.Message;

import com.sympnet.app.network.WebSocketManager;

public abstract class BaseActivity extends AppCompatActivity implements WebSocketManager.ChatListener {

    protected WebSocketManager webSocketManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webSocketManager = WebSocketManager.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webSocketManager != null) {
            webSocketManager.addChatListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webSocketManager != null) {
            webSocketManager.removeChatListener(this);
        }
    }

    protected void setupBottomNav() {
        ImageView navHome     = findViewById(R.id.nav_home_icon);
        ImageView navChat     = findViewById(R.id.nav_chat_icon);
        ImageView navProfile  = findViewById(R.id.nav_profile_icon);
        ImageView navCalendar = findViewById(R.id.nav_calendar_icon);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, ActivityHome.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            });
        }

        if (navCalendar != null) {
            navCalendar.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("TARGET_FRAGMENT", "SCHEDULE")
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            });
        }

        if (navChat != null) {
            navChat.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("TARGET_FRAGMENT", "CHAT")
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("TARGET_FRAGMENT", "PROFILE")
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            });
        }
    }

    // ── ChatListener ──────────────────────────────────────────────────────

    @Override public void onConnected() {}
    @Override public void onDisconnected() {}
    @Override public void onNewMessage(Message message) {}
    @Override public void onMessageDelivered(String messageId) {}
    @Override public void onMessageRead(String messageId) {}
    @Override public void onTyping(String userId, String userName, boolean isTyping) {}


    @Override public void onUpdateConversationList() {}
    @Override public void onConversationCreated(String doctorName, String appointmentDate) {}
}

