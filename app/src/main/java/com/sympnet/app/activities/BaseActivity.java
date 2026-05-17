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
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.sympnet.app.utils.LocaleHelper.onAttach(newBase));
    }

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
        checkUnreadMessagesBadge();
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

    protected void checkUnreadMessagesBadge() {
        final android.view.View chatBadge = findViewById(R.id.nav_chat_badge);
        if (chatBadge == null) return;

        com.sympnet.app.utils.SessionManager session = com.sympnet.app.utils.SessionManager.getInstance(this);
        String token = session.getUserToken();
        if (token == null || token.isEmpty()) {
            chatBadge.setVisibility(android.view.View.GONE);
            return;
        }

        com.sympnet.app.network.ApiService apiService = com.sympnet.app.network.ApiClient.getClient().create(com.sympnet.app.network.ApiService.class);
        apiService.getConversations("Bearer " + token).enqueue(new retrofit2.Callback<java.util.List<com.sympnet.app.model.Conversation>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.sympnet.app.model.Conversation>> call, retrofit2.Response<java.util.List<com.sympnet.app.model.Conversation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean hasUnread = false;
                    for (com.sympnet.app.model.Conversation conv : response.body()) {
                        if (conv.getUnreadCount() > 0) {
                            hasUnread = true;
                            break;
                        }
                    }
                    final boolean finalHasUnread = hasUnread;
                    runOnUiThread(() -> chatBadge.setVisibility(finalHasUnread ? android.view.View.VISIBLE : android.view.View.GONE));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.sympnet.app.model.Conversation>> call, Throwable t) {
                Log.e("BaseActivity", "checkUnreadMessagesBadge failed", t);
            }
        });
    }

    // ── ChatListener ──────────────────────────────────────────────────────

    @Override public void onConnected() {}
    @Override public void onDisconnected() {}
    @Override
    public void onNewMessage(Message message) {
        runOnUiThread(this::checkUnreadMessagesBadge);
    }
    @Override public void onMessageDelivered(String messageId) {}
    @Override public void onMessageRead(String messageId) {}
    @Override public void onTyping(String userId, String userName, boolean isTyping) {}

    @Override
    public void onUpdateConversationList() {
        runOnUiThread(this::checkUnreadMessagesBadge);
    }
    @Override
    public void onConversationCreated(String doctorName, String appointmentDate) {
        runOnUiThread(this::checkUnreadMessagesBadge);
    }
}

