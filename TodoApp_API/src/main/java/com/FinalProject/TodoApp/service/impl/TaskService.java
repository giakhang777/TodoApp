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
        // Lấy user
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: " + dto.getUserId()));

        // Lấy project
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + dto.getProjectId()));

        // Lấy label nếu có
        Label label = null;
        if (dto.getLabelId() != null) {
            label = labelRepository.findById(dto.getLabelId())
                    .orElseThrow(() -> new DataNotFoundException("Label not found with ID: " + dto.getLabelId()));
        }

        // Tạo task mới
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
        // Tìm task cần update
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + id));

        // Lấy lại thông tin project
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + dto.getProjectId()));

        // Lấy label nếu có
        Label label = null;
        if (dto.getLabelId() != null) {
            label = labelRepository.findById(dto.getLabelId())
                    .orElseThrow(() -> new DataNotFoundException("Label not found with ID: " + dto.getLabelId()));
        }

        // Không thay đổi userId (chỉ cho tạo ban đầu), tránh lỗi gán sai user

        // Cập nhật các trường
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

        // Xoá tất cả SubTask liên quan đến Task
        subTaskRepository.deleteByTaskId(id);  // Phương thức này xoá các SubTask liên quan

        // Sau đó xoá Task
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
        // Cấu hình ModelMapper ánh xạ các trường cần thiết
        TaskResponseDTO dto = modelMapper.map(task, TaskResponseDTO.class);

        // Ánh xạ thủ công tên của label, nếu có
        if (task.getLabel() != null) {
            dto.setLabel(task.getLabel().getTitle());
        } else {
            dto.setLabel(null);
        }

        // Lấy tên của project từ đối tượng project và gán cho dto
        if (task.getProject() != null) {
            dto.setProject(task.getProject().getName());  // Lấy tên của project
        } else {
            dto.setProject(null);  // Nếu không có project thì set null
        }

        // Lấy danh sách subTasks từ SubTaskRepository
        List<SubResponseDTO> subTasks = subTaskRepository.findByTaskId(task.getId()).stream()
                .map(subTask -> modelMapper.map(subTask, SubResponseDTO.class))
                .collect(Collectors.toList());

        dto.setSubTasks(subTasks);

        return dto;
    }
    @Override
    public List<TaskResponseDTO> getAllByDate(LocalDate date, Integer userId) {
        List<Task> tasks = taskRepository.findByDueDateAndUserId(date,userId);

        return tasks.stream()
                .map(this::mapTaskToResponseDTO)
                .collect(Collectors.toList());
    }
}
