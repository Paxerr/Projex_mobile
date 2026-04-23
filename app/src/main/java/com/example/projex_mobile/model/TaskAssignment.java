package com.example.projex_mobile.model;

// TaskAssignment.java
public class TaskAssignment {
    private int TaskId;
    private int UserId;
    private String AssignedAt;

    public TaskAssignment() {}

    public int getTaskId() { return TaskId; }
    public void setTaskId(int taskId) { TaskId = taskId; }

    public int getUserId() { return UserId; }
    public void setUserId(int userId) { UserId = userId; }

    public String getAssignedAt() { return AssignedAt; }
    public void setAssignedAt(String assignedAt) { AssignedAt = assignedAt; }
}
