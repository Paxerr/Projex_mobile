package com.example.projex_mobile.objects;

public class RecentAccessResponse {
    private int taskId;
    private String accessAt;

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public String getAccessAt() { return accessAt; }
    public void setAccessAt(String accessAt) { this.accessAt = accessAt; }
}