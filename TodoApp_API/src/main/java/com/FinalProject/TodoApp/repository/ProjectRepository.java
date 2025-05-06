package com.FinalProject.TodoApp.repository;

import com.FinalProject.TodoApp.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    List<Project> findByUserId(Integer userId);
}
