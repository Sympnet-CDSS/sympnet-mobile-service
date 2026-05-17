package com.sympnet.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sympnet.app.R;
import com.sympnet.app.adapters.ConversationAdapter;
import com.sympnet.app.model.Conversation;
import com.sympnet.app.model.Message;

import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;
import com.sympnet.app.network.WebSocketManager;
import com.sympnet.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListActivity extends AppCompatActivity implements WebSocketManager.ChatListener {

    private static final String TAG = "ChatListActivity";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ConversationAdapter adapter;
    private final List<Conversation> conversations = new ArrayList<>();
    private WebSocketManager webSocketManager;
    private final android.os.Handler refreshHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        initViews();
        setupWebSocket();
        loadConversations();
        setupPeriodicRefresh();
    }

    private void setupPeriodicRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadConversations();
                refreshHandler.postDelayed(this, 15000); // Rafraîchir toutes les 15s
            }
        };
        refreshHandler.postDelayed(refreshRunnable, 15000);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        adapter = new ConversationAdapter(conversations, conversation -> {
            // --- DIAGNOSTIC LOG ---
            Log.d(TAG, "========================================");
            Log.d(TAG, "TENTATIVE D'OUVERTURE CHAT");
            Log.d(TAG, "ID CONVERSATION : " + conversation.getId());
            Log.d(TAG, "NOM PARTENAIRE  : " + conversation.getOtherUserName());
            Log.d(TAG, "========================================");

            if (conversation.getId() == null || conversation.getId().isEmpty()) {
                Log.e(TAG, "ERREUR CRITIQUE : L'ID est null !");
                Toast.makeText(this, "ID de conversation invalide (null)", Toast.LENGTH_SHORT).show();
                return;
            }
            ChatDetailActivity.start(this, conversation);
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupWebSocket() {
        webSocketManager = WebSocketManager.getInstance();
        String token = SessionManager.getInstance(this).getUserToken();
        webSocketManager.connect(token, this);
    }

    private void loadConversations() {
        progressBar.setVisibility(View.VISIBLE);

        String token = SessionManager.getInstance(this).getUserToken();

        ApiClient.getClient()
                .create(ApiService.class)
                .getConversations("Bearer " + token)
                .enqueue(new retrofit2.Callback<List<Conversation>>() {
                    @Override
                    public void onResponse(Call<List<Conversation>> call,
                                           Response<List<Conversation>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            conversations.clear();
                            conversations.addAll(response.body());
                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(
                                    conversations.isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            Log.e("ChatList", "Erreur API: " + response.code());
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Conversation>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("ChatList", "Erreur réseau: " + t.getMessage());
                        Toast.makeText(ChatListActivity.this,
                                "Erreur réseau", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showMockData() {
        Log.w(TAG, "MODE TEST activé car l'API a échoué.");
        Conversation conv1 = new Conversation();
        conv1.setId("id_test_valide_123");
        conv1.setOtherUserId("101");
        conv1.setOtherUserName("Dr. Martin (Test)");
        conv1.setOtherUserRole("Cardiologue");
        conv1.setLastMessage("Bonjour, comment allez-vous ?");
        conversations.add(conv1);
        updateUI();
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(conversations.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override public void onConnected() {}
    @Override public void onDisconnected() {}

    @Override
    public void onNewMessage(Message message) {
        runOnUiThread(() -> {
            for (Conversation conv : conversations) {
                if (conv.getOtherUserId().equals(message.getSenderId())) {
                    conv.setLastMessage(message.getContent());
                    conv.setUnreadCount(conv.getUnreadCount() + 1);
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        });
    }

    @Override public void onMessageDelivered(String messageId) {}
    @Override public void onMessageRead(String messageId) {}
    @Override public void onTyping(String userId, String userName, boolean isTyping) {}


    @Override
    public void onUpdateConversationList() {
        runOnUiThread(this::loadConversations);
    }

    @Override
    public void onConversationCreated(String doctorName, String appointmentDate) {
        runOnUiThread(() -> {
            loadConversations();
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Info")
                    .setMessage("Nouvelle conversation avec Dr. " + doctorName)
                    .setPositiveButton("Ok", null)
                    .show();
        });
    }
}
