package com.sympnet.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
// import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sympnet.app.R;
import com.sympnet.app.adapters.MessageAdapter;
import com.sympnet.app.model.Conversation;
import com.sympnet.app.model.Message;

import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;
import com.sympnet.app.network.WebSocketManager;
import com.sympnet.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDetailActivity extends BaseActivity {

    private static final String TAG = "ChatDetailActivity";
    private static final String EXTRA_CONVERSATION = "conversation";
    public static final String EXTRA_DOCTOR_ID = "doctorId";
    public static final String EXTRA_DOCTOR_NAME = "doctorName";

    private RecyclerView recyclerView;
    private EditText etMessage;
    private TextView tvTyping, tvHeaderName, tvHeaderInitials;
    private View headerChat;

    private MessageAdapter messageAdapter;
    private final List<Message> messageList = new ArrayList<>();
    private Conversation conversation;
    private String currentUserId;
    private String currentUserName;
    private WebSocketManager webSocketManager;
    private final Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingTimeoutRunnable;
    private boolean isTyping = false;
    private ApiService apiService;

    public static void start(Context context, Conversation conversation) {
        Intent intent = new Intent(context, ChatDetailActivity.class);
        intent.putExtra(EXTRA_CONVERSATION, conversation);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        conversation = (Conversation) getIntent().getSerializableExtra(EXTRA_CONVERSATION);
        
        // DIAGNOSTIC LOG
        if (conversation != null) {
            Log.d(TAG, "Conversation reçue - ID: " + conversation.getId() + " Partenaire: " + conversation.getOtherUserName());
        } else {
            Log.e(TAG, "ERREUR : Objet Conversation null dans l'Intent !");
        }

        if (conversation == null) {
            String doctorId = getIntent().getStringExtra(EXTRA_DOCTOR_ID);
            String docName  = getIntent().getStringExtra(EXTRA_DOCTOR_NAME);
            if (doctorId != null) {
                createOrGetConversation(doctorId, docName);
                // Return for now, createOrGetConversation will re-initialize
                return;
            } else {
                Toast.makeText(this, "Données de conversation manquantes", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        currentUserId   = SessionManager.getInstance(this).getCurrentUserId();
        currentUserName = SessionManager.getInstance(this).getCurrentUserName();
        apiService      = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupWebSocket();
        loadMessages();
        markConversationAsRead();
    }

    private void createOrGetConversation(String doctorId, String doctorName) {
        String token = SessionManager.getInstance(this).getUserToken();
        Map<String, String> body = new HashMap<>();
        body.put("DoctorId", doctorId);

        apiService.createConversation("Bearer " + token, body).enqueue(new Callback<Conversation>() {
            @Override
            public void onResponse(@NonNull Call<Conversation> call, @NonNull Response<Conversation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    conversation = response.body();
                    initViews();
                    setupWebSocket();
                    loadMessages();
                } else {
                    Toast.makeText(ChatDetailActivity.this, "Erreur création conversation", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Conversation> call, @NonNull Throwable t) {
                Toast.makeText(ChatDetailActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void initViews() {
        headerChat       = findViewById(R.id.headerChat);
        tvHeaderName     = findViewById(R.id.tvHeaderName);
        tvHeaderInitials = findViewById(R.id.tvHeaderInitials);
        ImageButton btnBack = findViewById(R.id.btnBack);

        if (conversation != null) {
            String name = conversation.getOtherUserName();
            tvHeaderName.setText(name != null ? name : "Chat");
            
            // Initiales
            if (name != null && !name.isEmpty()) {
                String[] parts = name.split(" ");
                String initials = "";
                if (parts.length > 0) initials += parts[0].charAt(0);
                if (parts.length > 1) initials += parts[1].charAt(0);
                tvHeaderInitials.setText(initials.toUpperCase());
            }
        }

        btnBack.setOnClickListener(v -> finish());

        recyclerView    = findViewById(R.id.recyclerView);
        etMessage       = findViewById(R.id.etMessage);
        tvTyping        = findViewById(R.id.tvTyping);

        ImageButton btnSend   = findViewById(R.id.btnSend);
        ImageButton btnAttach = findViewById(R.id.btnAttach);

        messageAdapter = new MessageAdapter(messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
        btnAttach.setOnClickListener(v -> showAttachmentOptions());

        setupTypingIndicator();
    }

    private void setupTypingIndicator() {
        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) startTyping();
            else          stopTyping();
        });
        typingTimeoutRunnable = () -> { if (isTyping) stopTyping(); };
    }

    private void setupWebSocket() {
        String convId = conversation.getId();
        if (convId == null) return;

        webSocketManager = WebSocketManager.getInstance();
        webSocketManager.addChatListener(this);
        webSocketManager.joinConsultation(convId);
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        String convId = conversation.getId();
        if (convId == null) return;

        // On ajoute le message localement pour une UI fluide
        Message message = new Message(currentUserId, conversation.getOtherUserId(), content);
        message.setId("temp_" + System.currentTimeMillis());
        message.setSenderName(currentUserName);
        message.setSentAt(new Date().toString());
        message.setDelivered(false);
        
        runOnUiThread(() -> {
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
            etMessage.setText("");
        });

        // ✅ Envoi via API REST (plus fiable que SignalR par ngrok)
        String token = SessionManager.getInstance(this).getUserToken();
        Map<String, Object> body = new HashMap<>();
        body.put("ReceiverId", conversation.getOtherUserId());
        body.put("Content", content);

        apiService.sendMessage("Bearer " + token, body).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ Message envoyé via REST");
                    message.setDelivered(true);
                    // On peut mettre à jour l'ID réel ici si nécessaire
                } else {
                    Log.e(TAG, "❌ Erreur envoi REST: " + response.code());
                    // Fallback SignalR si l'API échoue
                    webSocketManager.sendMessage(convId, content, false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Échec réseau REST: " + t.getMessage());
                // Fallback SignalR
                webSocketManager.sendMessage(convId, content, false);
            }
        });

        stopTyping();
    }

    private void loadMessages() {
        String convId = conversation.getId();
        if (convId == null || convId.isEmpty()) {
            Toast.makeText(this, "ID de conversation invalide", Toast.LENGTH_LONG).show();
            Log.e(TAG, "conversation.getId() est null — objet: " + conversation.getOtherUserName());
            return;
        }

        String token = SessionManager.getInstance(this).getUserToken();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getConversationMessages("Bearer " + token, convId) // ✅ token ajouté
                .enqueue(new Callback<List<Message>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Message>> call,
                                           @NonNull Response<List<Message>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Ne vider que si on est au premier chargement
                            if (messageList.isEmpty()) {
                                messageList.addAll(response.body());
                                messageAdapter.notifyDataSetChanged();
                            } else {
                                // Merging simple: ajouter seulement ceux qu'on n'a pas
                                for (Message m : response.body()) {
                                    boolean exists = false;
                                    for (Message existing : messageList) {
                                        if (existing.getId() != null && existing.getId().equals(m.getId())) {
                                            exists = true; break;
                                        }
                                    }
                                    if (!exists) messageList.add(m);
                                }
                                messageAdapter.notifyDataSetChanged();
                            }
                            
                            if (!messageList.isEmpty())
                                recyclerView.scrollToPosition(messageList.size() - 1);
                        } else {
                            Log.e(TAG, "Erreur " + response.code() + " pour convId=" + convId);
                            Toast.makeText(ChatDetailActivity.this,
                                    "Erreur " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Message>> call,
                                          @NonNull Throwable t) {
                        Log.e(TAG, "Echec réseau", t);
                        Toast.makeText(ChatDetailActivity.this,
                                "Erreur réseau", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startTyping() {
        String convId = conversation.getId();
        if (!isTyping && convId != null) {
            isTyping = true;
            webSocketManager.sendTyping(convId, true);
            typingHandler.removeCallbacks(typingTimeoutRunnable);
            typingHandler.postDelayed(typingTimeoutRunnable, 3000);
        }
    }

    private void stopTyping() {
        String convId = conversation.getId();
        if (isTyping && convId != null) {
            isTyping = false;
            webSocketManager.sendTyping(convId, false);
            typingHandler.removeCallbacks(typingTimeoutRunnable);
        }
    }

    private void markConversationAsRead() {
        for (Message message : messageList) {
            if (message != null && !currentUserId.equals(message.getSenderId()) && !message.isRead()) {
                webSocketManager.markAsRead(message.getId(), message.getSenderId());
                message.setRead(true);
            }
        }
    }


    private void showAttachmentOptions() {
        String[] options = { getString(R.string.photo), getString(R.string.gallery), getString(R.string.audio), getString(R.string.cancel) };
        new MaterialAlertDialogBuilder(this).setTitle(R.string.attach_file).setItems(options, (dialog, which) -> {}).show();
    }

    @Override public void onConnected() {}
    @Override public void onDisconnected() {}

    @Override
    public void onNewMessage(Message message) {
        runOnUiThread(() -> {
            if (message != null && message.getSenderId() != null) {
                // Éviter les doublons : si c'est nous qui avons envoyé le message, on l'ignore
                if (message.getSenderId().equals(SessionManager.getInstance(ChatDetailActivity.this).getCurrentUserId())) {
                    return;
                }

                if (message.getSenderId().equals(conversation.getOtherUserId())) {
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                    webSocketManager.markAsRead(conversation.getId());
                }
            }
        });
    }


    @Override
    public void onMessageDelivered(String messageId) {
        updateMessageStatus(messageId, msg -> msg.setDelivered(true));
    }

    @Override
    public void onMessageRead(String messageId) {
        updateMessageStatus(messageId, msg -> msg.setRead(true));
    }

    @Override
    public void onTyping(String userId, String userName, boolean isTyping) {
        runOnUiThread(() -> {
            if (isTyping) {
                tvTyping.setVisibility(View.VISIBLE);
                tvTyping.setText(getString(R.string.user_typing, userName));
            } else {
                tvTyping.setVisibility(View.GONE);
            }
        });
    }


    
    @Override
    public void onUpdateConversationList() {
        // Optionnel : recharger les messages si on est dans la conversation
        runOnUiThread(this::loadMessages);
    }

    @Override
    public void onConversationCreated(String doctorName, String appointmentDate) {}

    private void updateMessageStatus(String messageId, MessageUpdateAction action) {
        runOnUiThread(() -> {
            for (int i = 0; i < messageList.size(); i++) {
                Message msg = messageList.get(i);
                if (msg != null && msg.getId() != null && msg.getId().equals(messageId)) {
                    action.update(msg);
                    messageAdapter.notifyItemChanged(i);
                    break;
                }
            }
        });
    }

    private interface MessageUpdateAction { void update(Message message); }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTyping();
        String convId = conversation.getId();
        if (convId != null) webSocketManager.leaveConsultation(convId);
        if (webSocketManager != null) webSocketManager.removeChatListener(this);
    }
}
