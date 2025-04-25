package com.FinalProject.TodoApp.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private Boolean completed;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;
}
