package com.FinalProject.TodoApp.entity;

import com.FinalProject.TodoApp.enums.TaskPriority;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private boolean isCompleted = false;

    private LocalDateTime createdTime = LocalDateTime.now();

    private LocalDate scheduledDate; // Ngày dự kiến làm task

    private LocalDateTime dueTime; // Deadline

    private LocalDateTime reminderTime; // Báo thức/nhắc nhở

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    // ---- Quan hệ với Project ----
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;  // Một Task thuộc về một Project

    // ---- Quan hệ với Label ----
    @ManyToOne
    @JoinColumn(name = "label_id")  // Mỗi Task thuộc về một Label
    private Label label;

    // ---- Quan hệ với User ----
    @ManyToOne
    @JoinColumn(name = "user_id")  // Mỗi Task thuộc về một User
    private User user;

    // ---- Quan hệ đệ quy: Subtask ----
    @ManyToOne
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL)
    private List<Task> subtasks = new ArrayList<>();

    // Getter & Setter
}
