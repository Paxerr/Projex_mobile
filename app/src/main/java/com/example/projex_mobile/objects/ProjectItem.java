package com.example.projex_mobile.objects;

public class ProjectItem {
    private int id;
    private String name;
    private String status;
    private int memberCount;
    private boolean favorite;

    public ProjectItem(int id, String name, String status, int memberCount, boolean favorite) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.memberCount = memberCount;
        this.favorite = favorite;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}