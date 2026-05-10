package com.sympnet.ui.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sympnet.R;
import com.sympnet.adapters.ConversationAdapter;
import com.sympnet.models.Conversation;
import com.sympnet.models.Message;
import com.sympnet.network.WebSocketManager;
import com.sympnet.utils.SessionManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatListActivity extends AppCompatActivity implements WebSocketManager.ChatListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ConversationAdapter adapter;
    private List<Conversation> conversations = new ArrayList<>();
    private WebSocketManager webSocketManager;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        currentUserId = SessionManager.getInstance(getApplicationContext()).getCurrentUserId();
        
        initViews();
        setupWebSocket();
        loadConversations();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        adapter = new ConversationAdapter(conversations, conversation -> {
            // Open chat detail
            ChatDetailActivity.start(this, conversation);
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupWebSocket() {
        webSocketManager = WebSocketManager.getInstance();
        String token = SessionManager.getInstance(this).getAuthToken();
        webSocketManager.connect(token, currentUserId, this);
    }

    private void loadConversations() {
        progressBar.setVisibility(View.VISIBLE);
        
        // TODO: Load from API
        // ApiService.getConversations().enqueue(...)
        
        // Mock data for demo
        Conversation conv1 = new Conversation();
        conv1.setId(1);
        conv1.setOtherUserId(101);
        conv1.setOtherUserName("Dr. Martin");
        conv1.setOtherUserRole("Cardiologue");
        conv1.setLastMessage("Bonjour, comment allez-vous ?");
        conv1.setLastMessageAt(new Date());
        conv1.setUnreadCount(2);
        conversations.add(conv1);
        
        Conversation conv2 = new Conversation();
        conv2.setId(2);
        conv2.setOtherUserId(102);
        conv2.setOtherUserName("Dr. Petit");
        conv2.setOtherUserRole("Dentiste");
        conv2.setLastMessage("Votre rendez-vous est confirmé");
        conv2.setLastMessageAt(new Date());
        conv2.setUnreadCount(0);
        conversations.add(conv2);
        
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        
        if (conversations.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            // WebSocket connected
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            // Try to reconnect
        });
    }

    @Override
    public void onNewMessage(Message message) {
        runOnUiThread(() -> {
            // Update conversation list
            for (Conversation conv : conversations) {
                if (conv.getOtherUserId() == message.getSenderId()) {
                    conv.setLastMessage(message.getContent());
                    conv.setLastMessageAt(message.getSentAt());
                    conv.setUnreadCount(conv.getUnreadCount() + 1);
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        });
    }

    @Override
    public void onMessageDelivered(int messageId) {}

    @Override
    public void onMessageRead(int messageId) {}

    @Override
    public void onTyping(int userId, String userName, boolean isTyping) {}

    @Override
    public void onIncomingCall(VideoCallSession call) {
        // Show incoming call dialog
    }

    @Override
    public void onCallAccepted(String callId) {}

    @Override
    public void onCallRejected(String callId) {}

    @Override
    public void onCallEnded(String callId) {}

    @Override
    public void onOffer(String callId, String sdp) {}

    @Override
    public void onAnswer(String callId, String sdp) {}

    @Override
    public void onIceCandidate(String callId, String candidate, int sdpMLineIndex, String sdpMid) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't disconnect here, keep connection for background
    }
}