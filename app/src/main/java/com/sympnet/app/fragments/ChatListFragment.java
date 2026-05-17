package com.sympnet.app.fragments;
import com.sympnet.app.activities.chat.ChatDetailActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ChatListFragment extends Fragment implements WebSocketManager.ChatListener {

    private static final String TAG = "ChatListFragment";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ConversationAdapter adapter;
    private final List<Conversation> conversations = new ArrayList<>();
    private WebSocketManager webSocketManager;
    private String currentUserId;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chat_list, container, false);

        View toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setVisibility(View.GONE);

        currentUserId = SessionManager.getInstance(requireContext()).getCurrentUserId();
        
        initViews(view);
        setupWebSocket();
        loadConversations();
        setupPeriodicRefresh();
        
        return view;
    }

    private void setupPeriodicRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    loadConversations();
                    refreshHandler.postDelayed(this, 15000); // Rafraîchir toutes les 15s
                }
            }
        };
        refreshHandler.postDelayed(refreshRunnable, 15000);
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        
        adapter = new ConversationAdapter(conversations, conversation -> {
            ChatDetailActivity.start(requireContext(), conversation);
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupWebSocket() {
        webSocketManager = WebSocketManager.getInstance();
        webSocketManager.addChatListener(this);
    }

    private void loadConversations() {
        if (!isAdded() || getContext() == null) return;
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        Context context = getContext();
        String token = SessionManager.getInstance(context).getUserToken();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getConversations("Bearer " + token).enqueue(new Callback<List<Conversation>>() {
            @Override
            public void onResponse(@NonNull Call<List<Conversation>> call, @NonNull Response<List<Conversation>> response) {
                if (!isAdded()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    conversations.clear();
                    conversations.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(conversations.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                } else {
                    Log.e(TAG, "Error loading conversations: " + response.code());
                    Context ctx = getContext();
                    if (ctx != null) {
                        Toast.makeText(ctx, "Erreur lors du chargement des conversations", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Conversation>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failure loading conversations", t);
                Context ctx = getContext();
                if (ctx != null) {
                    Toast.makeText(ctx, "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webSocketManager != null) {
            webSocketManager.addChatListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webSocketManager != null) {
            webSocketManager.removeChatListener(this);
        }
    }

    @Override public void onConnected() {}
    @Override public void onDisconnected() {}

    @Override
    public void onNewMessage(Message message) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                boolean found = false;
                for (Conversation conv : conversations) {
                    if (conv.getOtherUserId().equals(message.getSenderId()) || conv.getOtherUserId().equals(message.getReceiverId())) {
                        conv.setLastMessage(message.getContent());
                        conv.setLastMessageAt(message.getSentAt());
                        conv.setUnreadCount(conv.getUnreadCount() + 1);
                        found = true;
                        break;
                    }
                }
                if (found) {
                    adapter.notifyDataSetChanged();
                } else {
                    // Si nouvelle conversation, recharger tout
                    loadConversations();
                }
            });
        }
    }

    @Override public void onMessageDelivered(String messageId) {}
    @Override public void onMessageRead(String messageId) {}
    @Override public void onTyping(String userId, String userName, boolean isTyping) {}


    @Override
    public void onUpdateConversationList() {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(this::loadConversations);
        }
    }

    @Override
    public void onConversationCreated(String doctorName, String appointmentDate) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(this::loadConversations);
        }
    }
}
