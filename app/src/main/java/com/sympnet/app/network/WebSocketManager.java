package com.sympnet.network;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.json.JSONObject;
import com.sympnet.models.Message;
import com.sympnet.models.VideoCallSession;
import java.util.concurrent.TimeUnit;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static WebSocketManager instance;
    private WebSocket webSocket;
    private String wsUrl = "wss://your-api-gateway.com/ws";
    private String authToken;
    private int currentUserId;
    private WebSocketListener listener;

    public interface ChatListener {
        void onConnected();
        void onDisconnected();
        void onNewMessage(Message message);
        void onMessageDelivered(int messageId);
        void onMessageRead(int messageId);
        void onTyping(int userId, String userName, boolean isTyping);
        
        // Call signaling
        void onIncomingCall(VideoCallSession call);
        void onCallAccepted(String callId);
        void onCallRejected(String callId);
        void onCallEnded(String callId);
        void onOffer(String callId, String sdp);
        void onAnswer(String callId, String sdp);
        void onIceCandidate(String callId, String candidate, int sdpMLineIndex, String sdpMid);
    }

    private ChatListener chatListener;

    private WebSocketManager() {}

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void connect(String token, int userId, ChatListener listener) {
        this.authToken = token;
        this.currentUserId = userId;
        this.chatListener = listener;

        OkHttpClient client = new OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Request request = new Request.Builder()
                .url(wsUrl + "?token=" + token + "&userId=" + userId)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket connected");
                if (chatListener != null) {
                    chatListener.onConnected();
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handleMessage(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // Handle binary if needed
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closing: " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closed");
                if (chatListener != null) {
                    chatListener.onDisconnected();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket error", t);
            }
        });
    }

    private void handleMessage(String text) {
        try {
            JSONObject json = new JSONObject(text);
            String type = json.getString("type");

            switch (type) {
                case "new_message":
                    handleNewMessage(json);
                    break;
                case "message_delivered":
                    handleMessageDelivered(json);
                    break;
                case "message_read":
                    handleMessageRead(json);
                    break;
                case "typing":
                    handleTyping(json);
                    break;
                case "incoming_call":
                    handleIncomingCall(json);
                    break;
                case "call_accepted":
                    handleCallAccepted(json);
                    break;
                case "call_rejected":
                    handleCallRejected(json);
                    break;
                case "call_ended":
                    handleCallEnded(json);
                    break;
                case "offer":
                    handleOffer(json);
                    break;
                case "answer":
                    handleAnswer(json);
                    break;
                case "ice_candidate":
                    handleIceCandidate(json);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message", e);
        }
    }

    private void handleNewMessage(JSONObject json) throws Exception {
        Message message = new Message();
        message.setId(json.getInt("id"));
        message.setConversationId(json.getInt("conversationId"));
        message.setSenderId(json.getInt("senderId"));
        message.setReceiverId(json.getInt("receiverId"));
        message.setSenderName(json.getString("senderName"));
        message.setContent(json.getString("content"));
        message.setAttachmentUrl(json.optString("attachmentUrl"));
        message.setSentAt(new java.util.Date(json.getLong("sentAt")));
        
        if (chatListener != null) {
            chatListener.onNewMessage(message);
        }
    }

    private void handleMessageDelivered(JSONObject json) throws Exception {
        int messageId = json.getInt("messageId");
        if (chatListener != null) {
            chatListener.onMessageDelivered(messageId);
        }
    }

    private void handleMessageRead(JSONObject json) throws Exception {
        int messageId = json.getInt("messageId");
        if (chatListener != null) {
            chatListener.onMessageRead(messageId);
        }
    }

    private void handleTyping(JSONObject json) throws Exception {
        int userId = json.getInt("userId");
        String userName = json.getString("userName");
        boolean isTyping = json.getBoolean("isTyping");
        if (chatListener != null) {
            chatListener.onTyping(userId, userName, isTyping);
        }
    }

    private void handleIncomingCall(JSONObject json) throws Exception {
        VideoCallSession call = new VideoCallSession();
        call.setCallId(json.getString("callId"));
        call.setCallerId(json.getInt("callerId"));
        call.setCallerName(json.getString("callerName"));
        call.setCallType(json.getString("callType"));
        call.setStatus("pending");
        
        if (chatListener != null) {
            chatListener.onIncomingCall(call);
        }
    }

    private void handleCallAccepted(JSONObject json) throws Exception {
        String callId = json.getString("callId");
        if (chatListener != null) {
            chatListener.onCallAccepted(callId);
        }
    }

    private void handleCallRejected(JSONObject json) throws Exception {
        String callId = json.getString("callId");
        if (chatListener != null) {
            chatListener.onCallRejected(callId);
        }
    }

    private void handleCallEnded(JSONObject json) throws Exception {
        String callId = json.getString("callId");
        if (chatListener != null) {
            chatListener.onCallEnded(callId);
        }
    }

    private void handleOffer(JSONObject json) throws Exception {
        String callId = json.getString("callId");
        String sdp = json.getString("sdp");
        if (chatListener != null) {
            chatListener.onOffer(callId, sdp);
        }
    }

    private void handleAnswer(JSONObject json) throws Exception {
        String callId = json.getString("callId");
        String sdp = json.getString("sdp");
        if (chatListener != null) {
            chatListener.onAnswer(callId, sdp);
        }
    }

    private void handleIceCandidate(JSONObject json) throws Exception {
        String callId = json.getString("callId");
        String candidate = json.getString("candidate");
        int sdpMLineIndex = json.getInt("sdpMLineIndex");
        String sdpMid = json.getString("sdpMid");
        if (chatListener != null) {
            chatListener.onIceCandidate(callId, candidate, sdpMLineIndex, sdpMid);
        }
    }

    // Send methods
    public void sendMessage(int receiverId, String content) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "send_message");
            json.put("receiverId", receiverId);
            json.put("content", content);
            json.put("sentAt", System.currentTimeMillis());
            send(json);
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
        }
    }

    public void sendTypingStatus(int receiverId, boolean isTyping) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "typing");
            json.put("receiverId", receiverId);
            json.put("isTyping", isTyping);
            send(json);
        } catch (Exception e) {
            Log.e(TAG, "Error sending typing status", e);
        }
    }

    public void markAsRead(int messageId, int senderId) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "mark_read");
            json.put("messageId", messageId);
            json.put("senderId", senderId);
            send(json);
        } catch (Exception e) {
            Log.e(TAG, "Error marking as read", e);
        }
    }

    // Call signaling methods
    public void startCall(int receiverId, String callType) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "start_call");
            json.put("receiverId", receiverId);
            json.put("callType", callType);
            send(json);
        } catch (Exception e) {
            Log.e(TAG, "Error starting call", e);
        }
    }

    public void acceptCall(String callId) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "accept_call");
            json.put("callId", callId);
            send(json);
        } catch (Exception e) {
            Log.e(TAG, "Error accepting call", e);
        }
    }

    public void rejectCall(String callId) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "reject_call");
            json.put("callId", callId);
            send(json);
        } catch (Exception e) {
            Log.e(TAG, "Error rejecting call", e);
        }
    }

    public void endCall(String callId) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "end_call");
            json.put("callId", callId);
            send(json);
        } catch (Exception e) {
            Log.e(TAG, "Error ending call", e);
        }
    }

    public void sendOffer(String callId, String sdp) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "offer");
            json.put("callId", callId);
            json.put("sdp", sdp);
            send(json);
        } catch (Exception e) {
            Log.e(TAG, "Error sending offer", e);
        }
    }

    public void sendAnswer(String callId, String sdp) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "answer");
            json.put("callId", callId);
            json.put("sdp", sdp);
            send(json);
        } catch (Exception e) {
            Log.e(TAG, "Error sending answer", e);
        }
    }

    public void sendIceCandidate(String callId, String candidate, int sdpMLineIndex, String sdpMid) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "ice_candidate");
            json.put("callId", callId);
            json.put("candidate", candidate);
            json.put("sdpMLineIndex", sdpMLineIndex);
            json.put("sdpMid", sdpMid);
            send(json);
        } catch (Exception e) {
            Log.e(TAG, "Error sending ICE candidate", e);
        }
    }

    private void send(JSONObject json) {
        if (webSocket != null) {
            webSocket.send(json.toString());
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "User disconnected");
        }
    }

    public boolean isConnected() {
        return webSocket != null;
    }
}