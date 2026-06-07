package com.example.projex_mobile.objects;

public class NotificationItem {
    public int id;
    public String title;
    public String message;
    public String avatarText;
    public String senderAvatarUrl;
    public String senderName;
    public boolean isUnread;
    public Integer projectId;
    public Integer taskId;
    public String createdAt;

    public NotificationItem(int id, String title, String message, String avatarText,
                            String senderName, String senderAvatarUrl,
                            boolean isUnread, Integer projectId, Integer taskId, String createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.senderName = senderName;
        this.senderAvatarUrl = senderAvatarUrl;
        this.avatarText = makeAvatarText(senderName);
        this.isUnread = isUnread;
        this.projectId = projectId;
        this.taskId = taskId;
        this.createdAt = createdAt;
    }

    private String makeAvatarText(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] words = name.trim().split("\\s+");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
        }
        String first = words[0].substring(0, 1);
        String last = words[words.length - 1].substring(0, 1);
        return (first + last).toUpperCase();
    }
}