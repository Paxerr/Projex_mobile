package com.example.projex_mobile.objects;

public class QuickAccessItem {
    private int id;
    private String name;
    private int iconRes;
    private String label;

    public QuickAccessItem(int id, String name, int iconRes, String label) {
        this.id = id;
        this.name = name;
        this.iconRes = iconRes;
        this.label = label;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getIconRes() { return iconRes; }
    public String getLabel() { return label; }

}