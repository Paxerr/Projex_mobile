package com.example.projex_mobile.objects;

import com.google.gson.annotations.SerializedName;

// User.java
public class User {
    @SerializedName("id")
    private int Id;

    @SerializedName("email")
    private String Email;

    @SerializedName("passwordHash")
    private String PasswordHash;

    @SerializedName("fullName")
    private String FullName;

    @SerializedName("phoneNumber")
    private String PhoneNumber;

    @SerializedName("avatarUrl")
    private String AvatarUrl;

    @SerializedName("isActive")
    private boolean IsActive;

    @SerializedName("createdAt")
    private String CreatedAt;

    @SerializedName("updatedAt")
    private String UpdatedAt;

    public User() {}

    public int getId() { return Id; }
    public void setId(int id) { Id = id; }

    public String getEmail() { return Email; }
    public void setEmail(String email) { Email = email; }

    public String getPasswordHash() { return PasswordHash; }
    public void setPasswordHash(String passwordHash) { PasswordHash = passwordHash; }

    public String getFullName() { return FullName; }
    public void setFullName(String fullName) { FullName = fullName; }

    public String getPhoneNumber() { return PhoneNumber; }
    public void setPhoneNumber(String phoneNumber) { PhoneNumber = phoneNumber; }

    public String getAvatarUrl() { return AvatarUrl; }
    public void setAvatarUrl(String avatarUrl) { AvatarUrl = avatarUrl; }

    public boolean isIsActive() { return IsActive; }
    public void setIsActive(boolean isActive) { IsActive = isActive; }

    public String getCreatedAt() { return CreatedAt; }
    public void setCreatedAt(String createdAt) { CreatedAt = createdAt; }

    public String getUpdatedAt() { return UpdatedAt; }
    public void setUpdatedAt(String updatedAt) { UpdatedAt = updatedAt; }
}