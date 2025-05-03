package com.FinalProject.TodoApp.repository;

import com.FinalProject.TodoApp.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabelRepository extends JpaRepository<Label, Integer> {
}
