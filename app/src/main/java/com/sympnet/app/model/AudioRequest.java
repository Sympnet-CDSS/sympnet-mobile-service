package com.sympnet.app.model;

import com.google.gson.annotations.SerializedName;

public class AudioRequest {
    
    @SerializedName("audio")
    private String audio;

    public AudioRequest() {}

    public AudioRequest(String audio) {
        this.audio = audio;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}
