package com.example.apptodo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private int id;
    private String username;
    private String email;
    private String gender;
    private boolean isActive;
    private String avatar;
}
