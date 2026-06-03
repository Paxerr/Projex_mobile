package com.example.projex_mobile.objects;

public class NotificationItem {
    public int id;
    public String title;
    public String message;
    public String avatarText;
    public boolean isUnread;

    public Integer projectId;
    public Integer taskId;
    public String createdAt;

    public NotificationItem(int id, String title, String message, String avatarText,
                            boolean isUnread, Integer projectId, Integer taskId, String createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.avatarText = avatarText;
        this.isUnread = isUnread;
        this.projectId = projectId;
        this.taskId = taskId;
        this.createdAt = createdAt;
    }
}