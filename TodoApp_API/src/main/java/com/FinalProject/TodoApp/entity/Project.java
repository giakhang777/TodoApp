package com.FinalProject.TodoApp.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // Một Project thuộc về một User

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<Task> tasks = new HashSet<>();  // Một Project có thể có nhiều Task

    // Quan hệ Many-to-Many với Label (Mỗi Project có thể có nhiều Label)
    @ManyToMany
    @JoinTable(
            name = "label_project",  // Tên bảng trung gian
            joinColumns = @JoinColumn(name = "project_id"),  // Khóa ngoại chỉ đến Project
            inverseJoinColumns = @JoinColumn(name = "label_id")  // Khóa ngoại chỉ đến Label
    )
    private Set<Label> labels = new HashSet<>();  // Một Project có thể có nhiều Label

    // Getter & Setter
}
