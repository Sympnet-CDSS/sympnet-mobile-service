package com.sympnet.app.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("token")
    private String token;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("userId")
    private String userId;

    @SerializedName("fullName")
    private String fullName;

    public String getToken()    { return token    != null ? token    : ""; }
    public String getEmail()    { return email    != null ? email    : ""; }
    public String getRole()     { return role     != null ? role     : ""; }
    public String getUserId()   { return userId   != null ? userId   : ""; }
    public String getFullName() { return fullName != null ? fullName : ""; }

    public void setToken(String v)    { token    = v; }
    public void setEmail(String v)    { email    = v; }
    public void setRole(String v)     { role     = v; }
    public void setUserId(String v)   { userId   = v; }
    public void setFullName(String v) { fullName = v; }
}