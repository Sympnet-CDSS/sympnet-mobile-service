package com.sympnet.app.network;

import android.util.Log;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.sympnet.app.model.Message;


import io.reactivex.rxjava3.core.Single;

public class WebSocketManager {
    private static final String TAG = "SignalR";
    private static WebSocketManager instance;
    private HubConnection hubConnection;
    private final java.util.List<ChatListener> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    // ✅ Correspond à app.MapHub<ChatHub>("/hubs/chat")
    private static final String HUB_URL = "https://faster-say-trimmer.ngrok-free.dev/hubs/chat"; // ngrok

    // ── Interface ─────────────────────────────────────────────────────────

    public interface ChatListener {
        void onConnected();

        void onDisconnected();

        void onNewMessage(Message message);

        void onMessageDelivered(String messageId);

        void onMessageRead(String messageId);

        void onTyping(String userId, String userName, boolean isTyping);



        void onUpdateConversationList();

        void onConversationCreated(String doctorName, String appointmentDate);

    }
    private WebSocketManager() {}

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) instance = new WebSocketManager();
        return instance;
    }

    // ── Connect ───────────────────────────────────────────────────────────

    public void connect(String token, ChatListener listener) {
        Log.d(TAG, "Attempting to connect with token: " + (token != null ? "present" : "NULL"));
        if (hubConnection != null &&
                hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
            Log.d(TAG, "Already connected");
            if (listener != null) listener.onConnected();
            return;
        }
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }

        hubConnection = HubConnectionBuilder
                .create(HUB_URL)
                .withHeader("ngrok-skip-browser-warning", "true")
                .withAccessTokenProvider(Single.just(token != null ? token : ""))
                .build();

        registerHandlers();

        hubConnection.onClosed(ex -> {
            Log.e(TAG, "Disconnected: " + (ex != null ? ex.getMessage() : "clean"));
            for (ChatListener l : listeners) l.onDisconnected();
        });

        hubConnection.start().subscribe(
                () -> {
                    Log.d(TAG, "✅ SignalR connected to " + HUB_URL);
                    for (ChatListener l : listeners) l.onConnected();
                },
                error -> Log.e(TAG, "❌ Connection failed to " + HUB_URL + " : " + error.getMessage())
        );
    }

    // ── Register server→client handlers ───────────────────────────────────

    private void registerHandlers() {

        hubConnection.on("ReceiveMessage",
                (senderId, senderName, senderRole, content, isVoice, sentAt) -> {
                    Message message = new Message();
                    message.setSenderId(senderId);
                    message.setSenderName(senderName);
                    message.setContent(content);
                    message.setSentAt(sentAt);
                    for (ChatListener l : listeners) l.onNewMessage(message);
                },
                String.class, String.class, String.class,
                String.class, Boolean.class, String.class);

        hubConnection.on("UserTyping", (senderName, isTyping) -> {
            for (ChatListener l : listeners) l.onTyping("", senderName, isTyping);
        }, String.class, Boolean.class);



        hubConnection.on("UpdateConversationList", () -> {
            for (ChatListener l : listeners) l.onUpdateConversationList();
        });

        hubConnection.on("MessagesRead", (conversationId) -> {
            for (ChatListener l : listeners) l.onUpdateConversationList();
        }, String.class);

        hubConnection.on("MessageDelivered", messageId -> {
            for (ChatListener l : listeners) l.onMessageDelivered(messageId);
        }, String.class);

        hubConnection.on("MessageRead", messageId -> {
            for (ChatListener l : listeners) l.onMessageRead(messageId);
        }, String.class);

        hubConnection.on("ConversationCreated", (doctorName, appointmentDate) -> {
            for (ChatListener l : listeners) l.onConversationCreated(doctorName, appointmentDate);
        }, String.class, String.class);
    }
    // ── Client→Server : correspond aux méthodes publiques du ChatHub ──────

    // ChatHub.SendMessage(string consultationId, string message, bool isVoice)
    public void sendMessage(String consultationId, String content, boolean isVoice) {
        invoke("SendMessage", consultationId, content, isVoice);
    }

    // ChatHub.JoinConsultation(string consultationId)
    public void joinConsultation(String consultationId) {
        invoke("JoinConsultation", consultationId);
    }

    // ChatHub.LeaveConsultation(string consultationId)
    public void leaveConsultation(String consultationId) {
        invoke("LeaveConsultation", consultationId);
    }

    // ChatHub.SendTyping(string consultationId, bool isTyping)
    public void sendTyping(String consultationId, boolean isTyping) {
        invoke("SendTyping", consultationId, isTyping);
    }



    public void markAsRead(String conversationId) {
        if (hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
            hubConnection.send("MarkAsRead", conversationId);
        }
    }


    public void markAsRead(String messageId, String senderId) {
        invoke("MarkAsRead", messageId, senderId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void invoke(String method, Object... args) {
        if (hubConnection != null &&
                hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
            Log.d(TAG, "Invoking server method: " + method);
            hubConnection.send(method, args);
        } else {
            String state = hubConnection != null ? hubConnection.getConnectionState().toString() : "NULL";
            Log.w(TAG, "Cannot invoke '" + method + "' — Current state: " + state);
        }
    }

    public void disconnect() {
        if (hubConnection != null) hubConnection.stop();
    }

    public boolean isConnected() {
        return hubConnection != null &&
                hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
    }

    public void addChatListener(ChatListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeChatListener(ChatListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    @Deprecated
    public void setChatListener(ChatListener listener) {
        listeners.clear();
        if (listener != null) listeners.add(listener);
    }
}
