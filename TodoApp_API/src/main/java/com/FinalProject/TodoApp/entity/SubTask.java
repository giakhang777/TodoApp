package com.FinalProject.TodoApp.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sub_task")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubTask extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private Boolean completed;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;
}
