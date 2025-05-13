package com.FinalProject.TodoApp.repository;

import com.FinalProject.TodoApp.entity.Project;
import com.FinalProject.TodoApp.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByProjectId(Integer projectId);
    List<Task> findByUserId(Integer userId);
    List<Task> findByDueDateAndUserId(LocalDate dueDate, Integer userId);
    @Modifying
    @Query("UPDATE Task t SET t.label = NULL WHERE t.label.id = ?1")
    void updateLabelIdToNull(Integer labelId);
}
