package com.example.apptodo.model.request;

import com.google.gson.annotations.SerializedName;

public class SubTaskRequest {
    private String title;
    @SerializedName("task_id")
    private Integer task;

    public SubTaskRequest(String title, Integer task) {
        this.title = title;
        this.task = task;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTask() {
        return task;
    }

    public void setTask(Integer task) {
        this.task = task;
    }
}
