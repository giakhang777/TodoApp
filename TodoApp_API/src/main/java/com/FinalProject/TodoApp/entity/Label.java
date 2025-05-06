package com.FinalProject.TodoApp.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "labels")
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Quan hệ Many-to-One với User (Mỗi Label được tạo bởi một User)
    @ManyToOne
    @JoinColumn(name = "user_id")  // Tạo khóa ngoại liên kết với bảng User
    private User user;  // Mỗi Label có một User tạo ra

    // Quan hệ One-to-Many với Task (Một Label có thể có nhiều Task)
    @OneToMany(mappedBy = "label")
    private List<Task> tasks = new ArrayList<>();

    // Quan hệ Many-to-Many với Project (Một Label có thể thuộc về nhiều Project)
    @ManyToMany
    @JoinTable(
            name = "label_project",  // Tên bảng trung gian
            joinColumns = @JoinColumn(name = "label_id"),  // Khóa ngoại chỉ đến Label
            inverseJoinColumns = @JoinColumn(name = "project_id")  // Khóa ngoại chỉ đến Project
    )
    private Set<Project> projects = new HashSet<>();  // Mỗi Label có thể thuộc về nhiều Project

    // Getter & Setter
}
