package com.example.apptodo.model.request;

import com.example.apptodo.model.BaseModel;

public class ProjectRequest {
    private Integer userId;
    private String name;
    private String color;

    public ProjectRequest(Integer userId, String name, String color) {
        this.userId = userId;
        this.name = name;
        this.color = color;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
