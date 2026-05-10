package com.sympnet.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sympnet.R;
import com.sympnet.adapters.MessageAdapter;
import com.sympnet.models.Conversation;
import com.sympnet.models.Message;
import com.sympnet.models.VideoCallSession;
import com.sympnet.network.WebSocketManager;
import com.sympnet.utils.SessionManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity implements WebSocketManager.ChatListener {

    private static final String EXTRA_CONVERSATION = "conversation";

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend, btnAttach, btnCall;
    private TextView tvTyping;
    private MaterialCardView typingIndicator;
    
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    private Conversation conversation;
    private int currentUserId;
    private String currentUserName;
    private WebSocketManager webSocketManager;
    private Handler typingHandler = new Handler();
    private Runnable typingTimeoutRunnable;
    private boolean isTyping = false;

    public static void start(AppCompatActivity activity, Conversation conversation) {
        Intent intent = new Intent(activity, ChatDetailActivity.class);
        intent.putExtra(EXTRA_CONVERSATION, conversation);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        conversation = (Conversation) getIntent().getSerializableExtra(EXTRA_CONVERSATION);
        if (conversation == null) {
            finish();
            return;
        }

        currentUserId = SessionManager.getInstance(this).getCurrentUserId();
        currentUserName = SessionManager.getInstance(this).getCurrentUserName();

        initViews();
        setupWebSocket();
        loadMessages();
        markConversationAsRead();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(conversation.getOtherUserName());
        
        if (conversation.getOtherUserRole() != null) {
            toolbar.setSubtitle(conversation.getOtherUserRole());
        }

        recyclerView = findViewById(R.id.recyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnAttach = findViewById(R.id.btnAttach);
        btnCall = findViewById(R.id.btnCall);
        tvTyping = findViewById(R.id.tvTyping);
        typingIndicator = findViewById(R.id.typingIndicator);

        messageAdapter = new MessageAdapter(messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
        btnAttach.setOnClickListener(v -> showAttachmentOptions());
        btnCall.setOnClickListener(v -> startVideoCall());

        setupTypingIndicator();
    }

    private void setupTypingIndicator() {
        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                startTyping();
            } else {
                stopTyping();
            }
        });

        typingTimeoutRunnable = () -> {
            if (isTyping) {
                stopTyping();
            }
        };
    }

    private void setupWebSocket() {
        webSocketManager = WebSocketManager.getInstance();
        webSocketManager.setChatListener(this);
    }

    private void loadMessages() {
        // TODO: Load from API
        // ApiService.getMessages(conversation.getOtherUserId()).enqueue(...)
        
        // Mock data for demo
        Message msg1 = new Message(101, currentUserId, "Bonjour docteur, j'ai une douleur");
        msg1.setId(1);
        msg1.setSenderName("Dr. Martin");
        msg1.setSentAt(new Date(System.currentTimeMillis() - 3600000));
        
        Message msg2 = new Message(currentUserId, 101, "Bonjour, décrivez votre douleur");
        msg2.setId(2);
        msg2.setSenderName(currentUserName);
        msg2.setSentAt(new Date(System.currentTimeMillis() - 3500000));
        
        Message msg3 = new Message(101, currentUserId, "C'est une douleur à la poitrine");
        msg3.setId(3);
        msg3.setSenderName("Dr. Martin");
        msg3.setSentAt(new Date(System.currentTimeMillis() - 3400000));
        
        messageList.add(msg1);
        messageList.add(msg2);
        messageList.add(msg3);
        
        messageAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        // Create local message
        Message message = new Message(currentUserId, conversation.getOtherUserId(), content);
        message.setId((int) System.currentTimeMillis());
        message.setSenderName(currentUserName);
        
        messageList.add(message);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
        etMessage.setText("");

        // Send via WebSocket
        webSocketManager.sendMessage(conversation.getOtherUserId(), content);
        
        stopTyping();
    }

    private void startTyping() {
        if (!isTyping) {
            isTyping = true;
            webSocketManager.sendTypingStatus(conversation.getOtherUserId(), true);
            
            typingHandler.removeCallbacks(typingTimeoutRunnable);
            typingHandler.postDelayed(typingTimeoutRunnable, 3000);
        }
    }

    private void stopTyping() {
        if (isTyping) {
            isTyping = false;
            webSocketManager.sendTypingStatus(conversation.getOtherUserId(), false);
            typingHandler.removeCallbacks(typingTimeoutRunnable);
        }
    }

    private void startVideoCall() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Appel vidéo")
                .setMessage("Voulez-vous lancer un appel vidéo avec " + conversation.getOtherUserName() + " ?")
                .setPositiveButton("Appeler", (dialog, which) -> {
                    VideoCallActivity.startAsCaller(this, conversation);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showAttachmentOptions() {
        String[] options = {"Photo", "Galerie", "Audio", "Annuler"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Joindre un fichier")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Photo
                            // Take photo
                            break;
                        case 1: // Gallery
                            // Pick from gallery
                            break;
                        case 2: // Audio
                            // Record audio
                            break;
                    }
                })
                .show();
    }

    private void markConversationAsRead() {
        for (Message message : messageList) {
            if (message.getSenderId() != currentUserId && !message.isRead()) {
                webSocketManager.markAsRead(message.getId(), message.getSenderId());
                message.setRead(true);
            }
        }
    }

    @Override
    public void onConnected() {}

    @Override
    public void onDisconnected() {}

    @Override
    public void onNewMessage(Message message) {
        runOnUiThread(() -> {
            // Check if message is from current conversation
            if (message.getSenderId() == conversation.getOtherUserId()) {
                messageList.add(message);
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
                
                // Mark as read
                webSocketManager.markAsRead(message.getId(), message.getSenderId());
            }
        });
    }

    @Override
    public void onMessageDelivered(int messageId) {
        runOnUiThread(() -> {
            for (Message msg : messageList) {
                if (msg.getId() == messageId) {
                    msg.setDelivered(true);
                    messageAdapter.notifyDataSetChanged();
                    break;
                }
            }
        });
    }

    @Override
    public void onMessageRead(int messageId) {
        runOnUiThread(() -> {
            for (Message msg : messageList) {
                if (msg.getId() == messageId) {
                    msg.setRead(true);
                    messageAdapter.notifyDataSetChanged();
                    break;
                }
            }
        });
    }

    @Override
    public void onTyping(int userId, String userName, boolean isTyping) {
        runOnUiThread(() -> {
            if (userId == conversation.getOtherUserId()) {
                if (isTyping) {
                    typingIndicator.setVisibility(View.VISIBLE);
                    tvTyping.setText(userName + " écrit...");
                } else {
                    typingIndicator.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onIncomingCall(VideoCallSession call) {
        runOnUiThread(() -> {
            if (call.getCallerId() == conversation.getOtherUserId()) {
                VideoCallActivity.startAsReceiver(this, call);
            }
        });
    }

    @Override
    public void onCallAccepted(String callId) {}

    @Override
    public void onCallRejected(String callId) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Appel refusé", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onCallEnded(String callId) {}

    @Override
    public void onOffer(String callId, String sdp) {}

    @Override
    public void onAnswer(String callId, String sdp) {}

    @Override
    public void onIceCandidate(String callId, String candidate, int sdpMLineIndex, String sdpMid) {}

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTyping();
    }
}