package com.FinalProject.TodoApp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

// Import đúng lớp Task của bạn, không phải của Spring
import com.FinalProject.TodoApp.entity.Task;

@Entity(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_name", unique = true)
    String username;

    @Column(name = "is_active")
    boolean isActive;

    @Column(name = "password")
    String password;

    @Column(name = "email")
    String email;

    @Column(name = "gender")
    String gender;

    @Column(name = "avatar")
    String avatar; // <- thuộc tính avatar mới

    // ---- Quan hệ với Task ----
    @OneToMany(mappedBy = "user")
    private List<Task> tasks;

    // ---- Quan hệ với Project ----
    @OneToMany(mappedBy = "user")
    private List<Project> projects;

    // ---- Quan hệ với Label (nếu người dùng có thể tạo label) ----
    @OneToMany(mappedBy = "user")
    private List<Label> labels;
}
