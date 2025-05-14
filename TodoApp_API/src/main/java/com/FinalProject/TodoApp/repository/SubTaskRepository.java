package com.FinalProject.TodoApp.repository;

import com.FinalProject.TodoApp.entity.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Integer> {
    List<SubTask> findByTaskId(Integer taskId);
    void deleteByTaskId(Integer taskId);
    @Query("SELECT COUNT(t) FROM SubTask t WHERE t.task.id = :taskId")
    int countTotalSubTasksByProjectId(Integer taskId);

    @Query("SELECT COUNT(t) FROM SubTask t WHERE t.task.id = :taskId AND t.completed = true")
    int countCompletedSubTasksByProjectId(Integer taskId);
}
