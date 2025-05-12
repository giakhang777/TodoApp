package com.example.apptodo.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UserResponse implements Serializable {
    private Integer id;
    private String username;
    private String email;
    private String gender;
    private boolean isActive;
    private String avatar;
    // Nên có constructor không tham số nếu dùng với Intent

}
