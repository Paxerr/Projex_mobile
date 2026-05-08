package com.example.projex_mobile.objects;

public class QuickAccessItem {
    private int id;
    private String title;
    private int iconRes;
    private String route;

    public QuickAccessItem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getIconRes() { return iconRes; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
}