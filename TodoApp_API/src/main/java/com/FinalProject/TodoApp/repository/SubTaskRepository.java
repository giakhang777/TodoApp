package com.FinalProject.TodoApp.repository;

import com.FinalProject.TodoApp.entity.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Integer> {
    List<SubTask> findByTaskId(Integer taskId);
}
