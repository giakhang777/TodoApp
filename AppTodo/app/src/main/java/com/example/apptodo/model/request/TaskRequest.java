package com.example.apptodo.model.request;

import com.google.gson.annotations.SerializedName;

public class TaskRequest {

    private String title;
    private String priority;

    @SerializedName("due_date")
    private String dueDate;

    private String description;
    private Boolean reminder;

    @SerializedName("reminder_time")
    private String reminderTime;

    @SerializedName("project_id")
    private Integer projectId;

    @SerializedName("user_id")
    private Integer userId;

    @SerializedName("label_id")
    private Integer labelId;

    // Constructor
    public TaskRequest(String title,Integer userId, String priority, String dueDate, String description,
                       Boolean reminder, String reminderTime, Integer projectId, Integer labelId) {
        this.title = title;
        this.priority = priority;
        this.userId = userId;
        this.dueDate = dueDate;
        this.description = description;
        this.reminder = reminder;
        this.reminderTime = reminderTime;
        this.projectId = projectId;
        this.labelId = labelId;
    }

    // Getters and Setters

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getReminder() { return reminder; }
    public void setReminder(Boolean reminder) { this.reminder = reminder; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public Integer getLabelId() { return labelId; }
    public void setLabelId(Integer labelId) { this.labelId = labelId; }
}

