package com.sympnet.app.model;

import com.google.gson.annotations.SerializedName;

public class QuickReply {
    
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("category")
    private String category;

    @SerializedName("icon")
    private String icon;

    @SerializedName("order")
    private int order;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("doctorSpeciality")
    private String doctorSpeciality;

    public QuickReply() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getDoctorSpeciality() { return doctorSpeciality; }
    public void setDoctorSpeciality(String doctorSpeciality) { this.doctorSpeciality = doctorSpeciality; }
}
