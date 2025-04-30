package com.FinalProject.TodoApp.service.impl;

import com.FinalProject.TodoApp.dto.request.ProjectRequestDTO;
import com.FinalProject.TodoApp.dto.request.TaskRequestDTO;
import com.FinalProject.TodoApp.dto.response.ProjectResponseDTO;
import com.FinalProject.TodoApp.dto.response.TaskResponseDTO;
import com.FinalProject.TodoApp.entity.Project;
import com.FinalProject.TodoApp.entity.Task;
import com.FinalProject.TodoApp.exception.DataNotFoundException;
import com.FinalProject.TodoApp.repository.ProjectRepository;
import com.FinalProject.TodoApp.repository.TaskRepository;
import com.FinalProject.TodoApp.service.ITaskService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService implements ITaskService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        Project project = projectRepository.findById(taskRequestDTO.getProjectId())
                .orElseThrow(() -> new DataNotFoundException("Project not found"));
        modelMapper.typeMap(TaskRequestDTO.class, Task.class)
                .addMappings(mapper -> mapper.skip(Task::setId));
        Task task = new Task();
        modelMapper.map(taskRequestDTO, task);
        task.setProject(project);
        task.setCompleted(false);
        Task savedTask = taskRepository.save(task);

        return modelMapper.map(savedTask, TaskResponseDTO.class);
    }

    @Override
    public TaskResponseDTO getTaskById(Integer id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + id));
        return modelMapper.map(task, TaskResponseDTO.class);
    }

    @Override
    public List<TaskResponseDTO> getTasksByProjectId(Integer projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);

        return tasks.stream()
                .map(task -> modelMapper.map(task, TaskResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponseDTO updateTask(Integer id, TaskRequestDTO dto) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + id));

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + dto.getProjectId()));

        modelMapper.map(dto, existingTask);
        existingTask.setProject(project);

        Task updated = taskRepository.save(existingTask);
        return modelMapper.map(updated, TaskResponseDTO.class);
    }

    @Override
    public void deleteTask(Integer id) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + id));
        taskRepository.delete(existing);
    }
    @Override
    public TaskResponseDTO changeTaskStatus(Integer taskId, Boolean completed) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + taskId));

        task.setCompleted(completed);
        Task updated = taskRepository.save(task);
        return modelMapper.map(updated, TaskResponseDTO.class);
    }

}
