package com.FinalProject.TodoApp.dto;

import lombok.Data;

@Data
    public class UserResponseDTO {
        private Integer userId;
        private String username;
        private String email;
        private String gender;
        private boolean isActive;
        private String avatar;
}
