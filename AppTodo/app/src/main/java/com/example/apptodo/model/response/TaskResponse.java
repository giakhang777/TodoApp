package com.example.apptodo.model.response;

import com.example.apptodo.model.BaseModel;

public class TaskResponse extends BaseModel {
    private Integer id;
    private String title;
    private String priority;
    private String dueDate;
    private String label;
    private String project;
    private String description;
    private Boolean reminder;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    private Boolean completed;

    public TaskResponse(Integer id,String project, String title, String priority, String dueDate, String label, String description, Boolean reminder, Boolean completed) {
        this.id = id;
        this.title = title;
        this.project = project;
        this.priority = priority;
        this.dueDate = dueDate;
        this.label = label;
        this.description = description;
        this.reminder = reminder;
        this.completed = completed;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getReminder() {
        return reminder;
    }

    public void setReminder(Boolean reminder) {
        this.reminder = reminder;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
