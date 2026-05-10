package com.sympnet.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class VideoCallSession {
    private String callId;
    private String callerId;
    private String callerName;
    private String callerRole;
    private String receiverId;
    private String receiverName;
    private String receiverRole;
    private String status; // pending, waiting, connected, ended, rejected, missed
    private String callType; // video, audio
    private Date startedAt;
    private Date connectedAt;
    private Date endedAt;

    @SerializedName(value = "duration", alternate = {"durationSeconds"})
    private Integer duration;

    public VideoCallSession() {}

    public VideoCallSession(String callerId, String receiverId, String callType) {
        this.callId = java.util.UUID.randomUUID().toString();
        this.callerId = callerId;
        this.receiverId = receiverId;
        this.callType = callType;
        this.status = "waiting";
        this.startedAt = new Date();
    }

    // Getters and Setters
    public String getCallId() { return callId; }
    public void setCallId(String callId) { this.callId = callId; }

    public String getCallerId() { return callerId; }
    public void setCallerId(String callerId) { this.callerId = callerId; }

    public String getCallerName() { return callerName; }
    public void setCallerName(String callerName) { this.callerName = callerName; }

    public String getCallerRole() { return callerRole; }
    public void setCallerRole(String callerRole) { this.callerRole = callerRole; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverRole() { return receiverRole; }
    public void setReceiverRole(String receiverRole) { this.receiverRole = receiverRole; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }

    public Date getStartedAt() { return startedAt; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }

    public Date getConnectedAt() { return connectedAt; }
    public void setConnectedAt(Date connectedAt) { this.connectedAt = connectedAt; }

    public Date getEndedAt() { return endedAt; }
    public void setEndedAt(Date endedAt) { this.endedAt = endedAt; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}