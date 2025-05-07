package com.FinalProject.TodoApp.repository;

import com.FinalProject.TodoApp.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabelRepository extends JpaRepository<Label, Integer> {
    List<Label> findByUserId(Integer userId);
}
