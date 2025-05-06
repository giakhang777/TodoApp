package com.FinalProject.TodoApp.service;

import com.FinalProject.TodoApp.dto.request.TaskRequestDTO;
import com.FinalProject.TodoApp.dto.response.TaskResponseDTO;

import java.util.List;

public interface ITaskService {
    TaskResponseDTO createTask(TaskRequestDTO dto);
    TaskResponseDTO getTaskById(Integer id);
    List<TaskResponseDTO> getTasksByProjectId(Integer projectId);
    TaskResponseDTO updateTask(Integer id, TaskRequestDTO dto);
    void deleteTask(Integer id);
    TaskResponseDTO changeTaskStatus(Integer taskId, Boolean completed);
}
