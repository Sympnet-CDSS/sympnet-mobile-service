package com.sympnet.app.model;

public class Doctor {

    private int id;
    private String name;
    private String specialty;
    private float rating;
    private double latitude;
    private double longitude;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getFullName() { return name; } // ← alias pour LaunchExample
    public String getSpecialty() { return specialty; }
    public float getRating() { return rating; }  // ← ajouté
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}