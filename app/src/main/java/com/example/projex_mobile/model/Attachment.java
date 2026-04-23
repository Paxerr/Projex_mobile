package com.example.projex_mobile.model;

// Attachment.java
public class Attachment {
    private int Id;
    private int TaskId;
    private String FileUrl;
    private String FileName;
    private int UploadedBy;
    private String UploadedAt;

    public Attachment() {}

    public int getId() { return Id; }
    public void setId(int id) { Id = id; }

    public int getTaskId() { return TaskId; }
    public void setTaskId(int taskId) { TaskId = taskId; }

    public String getFileUrl() { return FileUrl; }
    public void setFileUrl(String fileUrl) { FileUrl = fileUrl; }

    public String getFileName() { return FileName; }
    public void setFileName(String fileName) { FileName = fileName; }

    public int getUploadedBy() { return UploadedBy; }
    public void setUploadedBy(int uploadedBy) { UploadedBy = uploadedBy; }

    public String getUploadedAt() { return UploadedAt; }
    public void setUploadedAt(String uploadedAt) { UploadedAt = uploadedAt; }
}
