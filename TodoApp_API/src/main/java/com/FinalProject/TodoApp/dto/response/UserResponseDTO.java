package com.FinalProject.TodoApp.dto.response;

import lombok.Data;

@Data
    public class UserResponseDTO {
        private Integer id;
        private String username;
        private String email;
        private String gender;
        private boolean isActive;
        private String avatar;
}
