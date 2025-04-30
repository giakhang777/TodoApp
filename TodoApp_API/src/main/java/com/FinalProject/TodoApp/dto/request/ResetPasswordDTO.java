package com.FinalProject.TodoApp.dto.request;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String email;
    private String password;
}
