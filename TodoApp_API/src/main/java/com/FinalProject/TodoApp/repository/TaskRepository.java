package com.FinalProject.TodoApp.repository;

import com.FinalProject.TodoApp.entity.Project;
import com.FinalProject.TodoApp.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByProjectId(Integer projectId);
    List<Task> findByUserId(Integer userId);
    List<Task> findByDueDate(LocalDate dueDate);
}
