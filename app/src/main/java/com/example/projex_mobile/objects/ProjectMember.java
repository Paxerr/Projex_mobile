package com.example.projex_mobile.objects;

import com.google.gson.annotations.SerializedName;

// ProjectMember.java
public class ProjectMember {
    @SerializedName("userId")
    private int UserId;

    @SerializedName("role")
    private String Role;

    @SerializedName("joinedAt")
    private String JoinedAt;

    @SerializedName("user")
    private User User;

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public String getJoinedAt() {
        return JoinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        JoinedAt = joinedAt;
    }

    public User getUser() {
        return User;
    }

    public void setUser(User user) {
        User = user;
    }

}
