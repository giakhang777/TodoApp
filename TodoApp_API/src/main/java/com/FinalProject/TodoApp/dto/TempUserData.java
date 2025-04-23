package com.FinalProject.TodoApp.dto;

import com.FinalProject.TodoApp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TempUserData {
    private User user;
    private String otp;
}
