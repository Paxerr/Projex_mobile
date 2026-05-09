package com.example.projex_mobile.objects;

public class DashboardOverview {
    private int myProjects;
    private int myTasks;
    private int inProgressTasks;
    private int completedTasks;
    private int overdueTasks;
    private int unreadNotifications;
    private double onTimeRate;

    public DashboardOverview() {}

    // Getters & Setters
    public int getMyProjects() { return myProjects; }
    public void setMyProjects(int myProjects) { this.myProjects = myProjects; }

    public int getMyTasks() { return myTasks; }
    public void setMyTasks(int myTasks) { this.myTasks = myTasks; }

    public int getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(int overdueTasks) { this.overdueTasks = overdueTasks; }

    public int getUnreadNotifications() { return unreadNotifications; }
    public void setUnreadNotifications(int unreadNotifications) { this.unreadNotifications = unreadNotifications; }

    public double getOnTimeRate() { return onTimeRate; }
    public void setOnTimeRate(double onTimeRate) { this.onTimeRate = onTimeRate; }
}