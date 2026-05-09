package com.example.projex_mobile.objects;

public class NotificationItem {
    public String title;
    public String message;
    public String ticket;
    public String avatarText;
    public boolean isUnread;

    public NotificationItem(String title, String message, String ticket, String avatarText, boolean isUnread) {
        this.title = title;
        this.message = message;
        this.ticket = ticket;
        this.avatarText = avatarText;
        this.isUnread = isUnread;
    }
}