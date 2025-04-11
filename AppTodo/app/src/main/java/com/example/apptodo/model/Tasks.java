package com.example.apptodo.model;

public class Tasks {
    private String title;
    private String description;
    private String status;
    private String time;
    private boolean isCompleted;
    private int logoResId;

    public Tasks(String title, String description, String status, String time, boolean isCompleted, int logoResId) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.time = time;
        this.isCompleted = isCompleted;
        this.logoResId = logoResId;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public int getLogoResId() {
        return logoResId;
    }
}
