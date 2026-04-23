package com.example.projex_mobile.model;

// User.java
public class User {
    private int Id;
    private String Email;
    private String PasswordHash;
    private String FullName;
    private String AvatarUrl;
    private boolean IsActive;
    private String CreatedAt;
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

    public String getAvatarUrl() { return AvatarUrl; }
    public void setAvatarUrl(String avatarUrl) { AvatarUrl = avatarUrl; }

    public boolean isIsActive() { return IsActive; }
    public void setIsActive(boolean isActive) { IsActive = isActive; }

    public String getCreatedAt() { return CreatedAt; }
    public void setCreatedAt(String createdAt) { CreatedAt = createdAt; }

    public String getUpdatedAt() { return UpdatedAt; }
    public void setUpdatedAt(String updatedAt) { UpdatedAt = updatedAt; }
}