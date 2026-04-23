package com.example.projex_mobile.model;

// Project.java
public class Project {
    private int Id;
    private String Name;
    private String Description;
    private int OwnerId;
    private String Status;
    private String StartDate;
    private String EndDate;
    private String CreatedAt;
    private String UpdatedAt;

    public Project() {}

    public int getId() { return Id; }
    public void setId(int id) { Id = id; }

    public String getName() { return Name; }
    public void setName(String name) { Name = name; }

    public String getDescription() { return Description; }
    public void setDescription(String description) { Description = description; }

    public int getOwnerId() { return OwnerId; }
    public void setOwnerId(int ownerId) { OwnerId = ownerId; }

    public String getStatus() { return Status; }
    public void setStatus(String status) { Status = status; }

    public String getStartDate() { return StartDate; }
    public void setStartDate(String startDate) { StartDate = startDate; }

    public String getEndDate() { return EndDate; }
    public void setEndDate(String endDate) { EndDate = endDate; }

    public String getCreatedAt() { return CreatedAt; }
    public void setCreatedAt(String createdAt) { CreatedAt = createdAt; }

    public String getUpdatedAt() { return UpdatedAt; }
    public void setUpdatedAt(String updatedAt) { UpdatedAt = updatedAt; }
}
