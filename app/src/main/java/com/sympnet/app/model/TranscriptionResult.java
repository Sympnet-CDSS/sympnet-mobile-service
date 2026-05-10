package com.sympnet.app.model;

import com.google.gson.annotations.SerializedName;

public class TranscriptionResult {
    
    @SerializedName("text")
    private String text;

    @SerializedName("success")
    private boolean success;

    @SerializedName("confidence")
    private double confidence;

    @SerializedName("error")
    private String error;

    public TranscriptionResult() {}

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
