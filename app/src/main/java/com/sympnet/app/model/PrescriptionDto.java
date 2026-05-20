package com.sympnet.app.model;

public class PrescriptionDto {
    private String id;
    private String title;
    private String date;
    private String doctorName;
    private String pdfUrl;

    public PrescriptionDto(String id, String title, String date, String doctorName, String pdfUrl) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.doctorName = doctorName;
        this.pdfUrl = pdfUrl;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getDoctorName() { return doctorName; }
    public String getPdfUrl() { return pdfUrl; }
}
