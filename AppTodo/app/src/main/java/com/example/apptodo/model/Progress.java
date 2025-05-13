package com.example.apptodo.model;

public class Progress {
    private String nameTask;
    private String nameProject;
    private String nameLabel;
    private String priority;
    private String description;
    private int progress;
    public Progress(String nameTask, String nameProject, String nameLabel, String priority, String description, int progress) {
        this.nameTask = nameTask;
        this.nameProject = nameProject;
        this.nameLabel = nameLabel;
        this.priority = priority;
        this.description = description;
        this.progress = progress;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNameTask() {
        return nameTask;
    }

    public void setNameTask(String nameTask) {
        this.nameTask = nameTask;
    }

    public String getNameProject() {
        return nameProject;
    }

    public void setNameProject(String nameProject) {
        this.nameProject = nameProject;
    }

    public String getNameLabel() {
        return nameLabel;
    }

    public void setNameLabel(String nameLabel) {
        this.nameLabel = nameLabel;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
