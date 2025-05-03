package com.FinalProject.TodoApp.service.impl;

import com.FinalProject.TodoApp.dto.request.ProjectRequestDTO;
import com.FinalProject.TodoApp.dto.request.TaskRequestDTO;
import com.FinalProject.TodoApp.dto.response.ProjectResponseDTO;
import com.FinalProject.TodoApp.dto.response.TaskResponseDTO;
import com.FinalProject.TodoApp.entity.Label;
import com.FinalProject.TodoApp.entity.Project;
import com.FinalProject.TodoApp.entity.Task;
import com.FinalProject.TodoApp.exception.DataNotFoundException;
import com.FinalProject.TodoApp.repository.LabelRepository;
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
    private LabelRepository labelRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        Project project = projectRepository.findById(taskRequestDTO.getProjectId())
                .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + taskRequestDTO.getProjectId()));

        Label label = null;
        if (taskRequestDTO.getLabelId() != null) {
            label = labelRepository.findById(taskRequestDTO.getLabelId())
                    .orElseThrow(() -> new DataNotFoundException("Label not found with ID: " + taskRequestDTO.getLabelId()));
        }

        // Tạo task thủ công để tránh lỗi mapping
        Task task = Task.builder()
                .title(taskRequestDTO.getTitle())
                .priority(taskRequestDTO.getPriority())
                .dueDate(taskRequestDTO.getDueDate())
                .description(taskRequestDTO.getDescription())
                .reminder(taskRequestDTO.getReminder())
                .reminderTime(taskRequestDTO.getReminderTime())
                .completed(false)
                .project(project)
                .label(label)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapTaskToResponseDTO(savedTask);
    }

    @Override
    public TaskResponseDTO updateTask(Integer id, TaskRequestDTO dto) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + id));

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + dto.getProjectId()));

        Label label = null;
        if (dto.getLabelId() != null) {
            label = labelRepository.findById(dto.getLabelId())
                    .orElseThrow(() -> new DataNotFoundException("Label not found with ID: " + dto.getLabelId()));
        }

        // Cập nhật thủ công để tránh lỗi
        existingTask.setTitle(dto.getTitle());
        existingTask.setPriority(dto.getPriority());
        existingTask.setDueDate(dto.getDueDate());
        existingTask.setDescription(dto.getDescription());
        existingTask.setReminder(dto.getReminder());
        existingTask.setReminderTime(dto.getReminderTime());
        existingTask.setProject(project);
        existingTask.setLabel(label);

        Task updatedTask = taskRepository.save(existingTask);
        return mapTaskToResponseDTO(updatedTask);
    }


    @Override
    public TaskResponseDTO getTaskById(Integer id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + id));
        return mapTaskToResponseDTO(task);
    }

    @Override
    public List<TaskResponseDTO> getTasksByProjectId(Integer projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .map(this::mapTaskToResponseDTO)
                .collect(Collectors.toList());
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
        return mapTaskToResponseDTO(updated);
    }

    private TaskResponseDTO mapTaskToResponseDTO(Task task) {
        TaskResponseDTO dto = modelMapper.map(task, TaskResponseDTO.class);

        if (task.getLabel() != null) {
            dto.setLabel(task.getLabel().getTitle());
        } else {
            dto.setLabel(null);
        }
        return dto;
    }
}

