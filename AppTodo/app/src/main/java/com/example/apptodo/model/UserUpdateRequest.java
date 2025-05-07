package com.example.apptodo.model;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String username;
    private String avatar;
}