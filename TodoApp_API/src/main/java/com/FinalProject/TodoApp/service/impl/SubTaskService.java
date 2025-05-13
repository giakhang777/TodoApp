package com.FinalProject.TodoApp.service.impl;

import com.FinalProject.TodoApp.dto.request.SubTaskRequestDTO;
import com.FinalProject.TodoApp.dto.response.SubResponseDTO;
import com.FinalProject.TodoApp.entity.SubTask;
import com.FinalProject.TodoApp.entity.Task;
import com.FinalProject.TodoApp.exception.DataNotFoundException;
import com.FinalProject.TodoApp.repository.SubTaskRepository;
import com.FinalProject.TodoApp.repository.TaskRepository;
import com.FinalProject.TodoApp.service.ISubTaskService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubTaskService implements ISubTaskService {
    @Autowired
    private SubTaskRepository subTaskRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public SubResponseDTO createSubTask(SubTaskRequestDTO dto) {
        Task task = taskRepository.findById(dto.getTask())
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + dto.getTask()));

        SubTask subTask = modelMapper.map(dto, SubTask.class);
        subTask.setTask(task);
        subTask.setCompleted(false); // default false when created

        SubTask saved = subTaskRepository.save(subTask);
        return modelMapper.map(saved, SubResponseDTO.class);
    }

    @Override
    public SubResponseDTO updateSubTask(Integer id, SubTaskRequestDTO dto) {
        SubTask existing = subTaskRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("SubTask not found with ID: " + id));

        existing.setTitle(dto.getTitle());

        if (!existing.getTask().getId().equals(dto.getTask())) {
            Task task = taskRepository.findById(dto.getTask())
                    .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + dto.getTask()));
            existing.setTask(task);
        }

        SubTask updated = subTaskRepository.save(existing);
        return modelMapper.map(updated, SubResponseDTO.class);
    }

    @Override
    public void deleteSubTask(Integer id) {
        SubTask subTask = subTaskRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("SubTask not found with ID: " + id));
        subTaskRepository.delete(subTask);
    }

    @Override
    public List<SubResponseDTO> getSubTasksByTaskId(Integer taskId) {
        List<SubTask> list = subTaskRepository.findByTaskId(taskId);
        return list.stream()
                .map(sub -> modelMapper.map(sub, SubResponseDTO.class))
                .collect(Collectors.toList());
    }
    @Override
    public SubResponseDTO changeSubTaskStatus(Integer subTaskId, Boolean completed) {
        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new DataNotFoundException("SubTask not found with ID: " + subTaskId));

        subTask.setCompleted(completed);
        SubTask updated = subTaskRepository.save(subTask);
        return modelMapper.map(updated, SubResponseDTO.class);
    }

}