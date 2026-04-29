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

    public int getId() { return id; }

    public String getName() {
        String f = firstName != null ? firstName : "";
        String l = lastName != null ? lastName : "";
        return (f + " " + l).trim();
    }

    public String getFullName() { return getName(); }
    public String getSpecialty() { return speciality != null ? speciality : ""; }
    public float getRating() { return rating; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    @SerializedName("address")
    private String address;

    public String getAddress() { return address != null ? address : ""; }
}