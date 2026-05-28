package com.example.projex_mobile.objects;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Task {

    @SerializedName("id")
    private int Id;

    @SerializedName("projectId")
    private int ProjectId;

    @SerializedName("project")
    private Project project;

    @SerializedName("title")
    private String Title;

    @SerializedName("description")
    private String Description;

    @SerializedName("status")
    private String Status;

    @SerializedName("priority")
    private int Priority;

    @SerializedName("dueDate")
    private String DueDate;

    @SerializedName("createdBy")
    private int CreatedBy;

    @SerializedName("createdAt")
    private String CreatedAt;

    @SerializedName("updatedAt")
    private String UpdatedAt;

    @SerializedName("statusUpdatedAt")
    private String StatusUpdatedAt;

    @SerializedName("isDeleted")
    private boolean IsDeleted;
    private List<TaskAssignment> assignees;

    public Task() {
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getProjectId() {
        return ProjectId;
    }

    public void setProjectId(int projectId) {
        ProjectId = projectId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public int getPriority() {
        return Priority;
    }

    public void setPriority(int priority) {
        Priority = priority;
    }

    public String getDueDate() {
        return DueDate;
    }

    public void setDueDate(String dueDate) {
        DueDate = dueDate;
    }

    public int getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(int createdBy) {
        CreatedBy = createdBy;
    }

    public String getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        CreatedAt = createdAt;
    }

    public String getUpdatedAt() {
        return UpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        UpdatedAt = updatedAt;
    }

    public String getStatusUpdatedAt() {
        return StatusUpdatedAt;
    }

    public void setStatusUpdatedAt(String statusUpdatedAt) {
        StatusUpdatedAt = statusUpdatedAt;
    }

    public boolean isIsDeleted() {
        return IsDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        IsDeleted = isDeleted;
    }
    public List<TaskAssignment> getAssignees() {
        return assignees;
    }

    public void setAssignees(
            List<TaskAssignment> assignees
    ) {
        this.assignees = assignees;
    }
}