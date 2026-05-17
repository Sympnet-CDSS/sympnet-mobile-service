package com.sympnet.app.model;

import com.google.gson.annotations.SerializedName;

public class Doctor {

    private int id;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("speciality")
    private String speciality;

    private float rating;
    private double latitude;
    private double longitude;

    @SerializedName("address")
    private String address;

    @SerializedName("userId")
    private String userId;

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getId() { return id; }

    public String getName() {
        String f = firstName != null ? firstName : "";
        String l = lastName  != null ? lastName  : "";
        return (f + " " + l).trim();
    }

    public String getFullName()    { return getName(); }
    public String getSpecialty()   { return speciality != null ? speciality : ""; }
    public float  getRating()      { return rating; }
    public double getLatitude()    { return latitude; }
    public double getLongitude()   { return longitude; }
    public String getFirstName()   { return firstName; }
    public String getLastName()    { return lastName; }
    public String getAddress()     { return address != null ? address : ""; }

    // ── Setters (nécessaires pour reconstruire depuis le cache JSON) ─────────

    public void setId(int id)                  { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName)   { this.lastName = lastName; }
    public void setSpeciality(String s)        { this.speciality = s; }
    public void setRating(float rating)        { this.rating = rating; }
    public void setLatitude(double latitude)   { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setAddress(String address)     { this.address = address; }
}