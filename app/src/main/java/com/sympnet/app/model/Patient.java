package com.sympnet.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Maps exactly to C# Patient entity returned by GET /api/patients/{userId}
 *
 *   public int      Id                { get; set; }
 *   public string   FirstName         { get; set; }
 *   public string   LastName          { get; set; }
 *   public string   PhoneNumber       { get; set; }
 *   public DateTime DateOfBirth       { get; set; }
 *   public string   Gender            { get; set; }
 *   public string   Address           { get; set; }
 *   public string   BloodType         { get; set; }
 *   public string   Allergies         { get; set; }
 *   public string   MedicalHistory    { get; set; }
 *   public int      ConsultationCount { get; set; }
 *   public Guid     UserId            { get; set; }
 */
public class Patient {

    @SerializedName("id")
    private int id;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("gender")
    private String gender;

    @SerializedName("address")
    private String address;

    @SerializedName("bloodType")
    private String bloodType;

    @SerializedName("allergies")
    private String allergies;

    @SerializedName("medicalHistory")
    private String medicalHistory;

    @SerializedName("consultationCount")
    private int consultationCount;

    @SerializedName("userId")
    private String userId;

    // ── Getters ───────────────────────────────────────────────────────────

    public int    getId()                { return id; }
    public String getFirstName()         { return firstName      != null ? firstName      : ""; }
    public String getLastName()          { return lastName       != null ? lastName       : ""; }
    public String getPhoneNumber()       { return phoneNumber    != null ? phoneNumber    : ""; }
    public String getDateOfBirth()       { return dateOfBirth    != null ? dateOfBirth    : ""; }
    public String getGender()            { return gender         != null ? gender         : ""; }
    public String getAddress()           { return address        != null ? address        : ""; }
    public String getBloodType()         { return bloodType      != null ? bloodType      : ""; }
    public String getAllergies()         { return allergies      != null ? allergies      : ""; }
    public String getMedicalHistory()    { return medicalHistory != null ? medicalHistory : ""; }
    public int    getConsultationCount() { return consultationCount; }
    public String getUserId()            { return userId         != null ? userId         : ""; }

    /** Combined display name used in ActivityHome and ProfileFragment */
    public String getFullName() {
        return (getFirstName() + " " + getLastName()).trim();
    }
}