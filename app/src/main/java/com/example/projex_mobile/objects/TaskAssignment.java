package com.example.projex_mobile.objects;

import com.google.gson.annotations.SerializedName;

public class TaskAssignment {

    private int userId;

    private String fullName;

    private String email;

    @SerializedName("assignedAt")
    private String assignedAt;

    public TaskAssignment() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(String assignedAt) {
        this.assignedAt = assignedAt;
    }
}