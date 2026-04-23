package com.example.projex_mobile.model;

// Task.java
public class Task {
    private int Id;
    private int ProjectId;
    private String Title;
    private String Description;
    private String Status;
    private int Priority;
    private String DueDate;
    private int CreatedBy;
    private String CreatedAt;
    private String UpdatedAt;
    private String StatusUpdatedAt;
    private boolean IsDeleted;

    public Task() {}

    public int getId() { return Id; }
    public void setId(int id) { Id = id; }

    public int getProjectId() { return ProjectId; }
    public void setProjectId(int projectId) { ProjectId = projectId; }

    public String getTitle() { return Title; }
    public void setTitle(String title) { Title = title; }

    public String getDescription() { return Description; }
    public void setDescription(String description) { Description = description; }

    public String getStatus() { return Status; }
    public void setStatus(String status) { Status = status; }

    public int getPriority() { return Priority; }
    public void setPriority(int priority) { Priority = priority; }

    public String getDueDate() { return DueDate; }
    public void setDueDate(String dueDate) { DueDate = dueDate; }

    public int getCreatedBy() { return CreatedBy; }
    public void setCreatedBy(int createdBy) { CreatedBy = createdBy; }

    public String getCreatedAt() { return CreatedAt; }
    public void setCreatedAt(String createdAt) { CreatedAt = createdAt; }

    public String getUpdatedAt() { return UpdatedAt; }
    public void setUpdatedAt(String updatedAt) { UpdatedAt = updatedAt; }

    public String getStatusUpdatedAt() { return StatusUpdatedAt; }
    public void setStatusUpdatedAt(String statusUpdatedAt) { StatusUpdatedAt = statusUpdatedAt; }

    public boolean isIsDeleted() { return IsDeleted; }
    public void setIsDeleted(boolean isDeleted) { IsDeleted = isDeleted; }
}
