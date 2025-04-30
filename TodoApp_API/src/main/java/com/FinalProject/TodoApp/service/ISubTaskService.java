package com.FinalProject.TodoApp.service;

import com.FinalProject.TodoApp.dto.request.SubTaskRequestDTO;
import com.FinalProject.TodoApp.dto.response.SubResponseDTO;
import com.FinalProject.TodoApp.entity.SubTask;

import java.util.List;

public interface ISubTaskService {
    SubResponseDTO createSubTask(SubTaskRequestDTO dto);
    List<SubResponseDTO> getSubTasksByTaskId(Integer taskId);
    void deleteSubTask(Integer subTaskId);
    SubResponseDTO updateSubTask(Integer subTaskId, SubTaskRequestDTO dto);
    SubResponseDTO changeSubTaskStatus(Integer subTaskId, Boolean completed);
}
