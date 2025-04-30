package com.FinalProject.TodoApp.dto.request;

import lombok.Data;

@Data
public class UserRegisterRequestDTO {
    private String username;
    private String email;
    private String password;
    private String gender;
    private boolean isActive;
}
