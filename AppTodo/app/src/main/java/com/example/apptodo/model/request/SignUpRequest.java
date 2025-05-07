package com.example.apptodo.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    private String username;
    private String email;
    private String password;
    private String gender;
    private boolean isActive;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor

    public static class ResetPassword {
        private String email;
        private String password;
    }
}
