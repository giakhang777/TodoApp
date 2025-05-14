package com.FinalProject.TodoApp.service.impl;

import com.FinalProject.TodoApp.dto.request.TaskRequestDTO;
import com.FinalProject.TodoApp.dto.response.SubResponseDTO;
import com.FinalProject.TodoApp.dto.response.TaskResponseDTO;
import com.FinalProject.TodoApp.entity.Task;
import com.FinalProject.TodoApp.entity.Project;
import com.FinalProject.TodoApp.entity.Label;
import com.FinalProject.TodoApp.entity.User;
import com.FinalProject.TodoApp.exception.DataNotFoundException;
import com.FinalProject.TodoApp.repository.*;
import com.FinalProject.TodoApp.service.ITaskService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private SubTaskRepository subTaskRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public TaskResponseDTO createTask(TaskRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: " + dto.getUserId()));
        Project project = null;
        if (dto.getProjectId() != null) {
            project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + dto.getProjectId()));
        }
        Label label = null;
        if (dto.getLabelId() != null) {
            label = labelRepository.findById(dto.getLabelId())
                    .orElseThrow(() -> new DataNotFoundException("Label not found with ID: " + dto.getLabelId()));
        }
        Task task = Task.builder()
                .title(dto.getTitle())
                .priority(dto.getPriority())
                .dueDate(dto.getDueDate())
                .description(dto.getDescription())
                .reminder(dto.getReminder())
                .reminderTime(dto.getReminderTime())
                .completed(false)
                .user(user)
                .project(project)
                .label(label)
                .build();
        Task savedTask = taskRepository.save(task);
        return mapTaskToResponseDTO(savedTask);
    }

    @Override
    public TaskResponseDTO updateTask(Integer id, TaskRequestDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + id));
        Project project = null;
        if (dto.getProjectId() != null) {
            project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + dto.getProjectId()));
        }
        Label label = null;
        if (dto.getLabelId() != null) {
            label = labelRepository.findById(dto.getLabelId())
                    .orElseThrow(() -> new DataNotFoundException("Label not found with ID: " + dto.getLabelId()));
        }
        task.setTitle(dto.getTitle());
        task.setPriority(dto.getPriority());
        task.setDueDate(dto.getDueDate());
        task.setDescription(dto.getDescription());
        task.setReminder(dto.getReminder());
        task.setReminderTime(dto.getReminderTime());
        task.setProject(project);
        task.setLabel(label);
        Task updatedTask = taskRepository.save(task);
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
    public List<TaskResponseDTO> getTasksByUserId(Integer userId) {
        List<Task> tasks = taskRepository.findByUserId(userId);
        return tasks.stream()
                .map(this::mapTaskToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTask(Integer id) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + id));
        subTaskRepository.deleteByTaskId(id);
        taskRepository.delete(existing);
    }

    @Override
    public TaskResponseDTO changeTaskStatus(Integer taskId, Boolean completed) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + taskId));
        task.setCompleted(completed);
        subTaskRepository.findByTaskId(taskId).forEach(subTask -> {
            subTask.setCompleted(completed);
            subTaskRepository.save(subTask);
        });
        Task updated = taskRepository.save(task);
        return mapTaskToResponseDTO(updated);
    }

    private TaskResponseDTO mapTaskToResponseDTO(Task task) {
        TypeMap<Task, TaskResponseDTO> typeMap = modelMapper.getTypeMap(Task.class, TaskResponseDTO.class);
        if (typeMap == null) {
            typeMap = modelMapper.createTypeMap(Task.class, TaskResponseDTO.class);
            typeMap.addMappings(mapper -> {
                mapper.skip(TaskResponseDTO::setProject);
                mapper.skip(TaskResponseDTO::setLabel);
                mapper.skip(TaskResponseDTO::setSubTasks);
            });
        }
        TaskResponseDTO dto = modelMapper.map(task, TaskResponseDTO.class);
        dto.setLabel(task.getLabel() != null ? task.getLabel().getTitle() : null);
        if (task.getProject() != null) {
            dto.setProject(task.getProject().getName());
        } else {
            dto.setProject(null);
        }
        List<SubResponseDTO> subTasks = subTaskRepository.findByTaskId(task.getId()).stream()
                .map(subTask -> modelMapper.map(subTask, SubResponseDTO.class))
                .collect(Collectors.toList());
        dto.setSubTasks(subTasks);
        dto.setCompletedSubTasks(subTaskRepository.countCompletedSubTasksByProjectId(task.getId()));
        dto.setTotalSubTasks(subTaskRepository.countTotalSubTasksByProjectId(task.getId()));
        return dto;
    }

    @Override
    public List<TaskResponseDTO> getAllByDate(LocalDate date, Integer userId) {
        List<Task> tasks = taskRepository.findByDueDateAndUserId(date, userId);
        return tasks.stream()
                .map(this::mapTaskToResponseDTO)
                .collect(Collectors.toList());
    }
}