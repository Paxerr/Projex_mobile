package com.example.projex_mobile.objects;

public class RecentItem {
    private int id;
    private String title;
    private String message;
    private String ticketCode;
    private String avatarText;
    private boolean isRead;
    private String createdAt;
    private String timeAgo;
    private String status;
    public RecentItem() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getAvatarText() {
        return avatarText;
    }

    public void setAvatarText(String avatarText) {
        this.avatarText = avatarText;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
