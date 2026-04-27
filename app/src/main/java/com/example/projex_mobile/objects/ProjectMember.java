package com.example.projex_mobile.objects;

// ProjectMember.java
public class ProjectMember {
    private int UserId;
    private int ProjectId;
    private String Role;
    private String JoinedAt;

    public ProjectMember() {}

    public int getUserId() { return UserId; }
    public void setUserId(int userId) { UserId = userId; }

    public int getProjectId() { return ProjectId; }
    public void setProjectId(int projectId) { ProjectId = projectId; }

    public String getRole() { return Role; }
    public void setRole(String role) { Role = role; }

    public String getJoinedAt() { return JoinedAt; }
    public void setJoinedAt(String joinedAt) { JoinedAt = joinedAt; }
}
