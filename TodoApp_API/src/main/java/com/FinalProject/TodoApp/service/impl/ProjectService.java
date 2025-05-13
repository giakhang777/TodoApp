package com.FinalProject.TodoApp.service.impl;

import com.FinalProject.TodoApp.dto.request.ProjectRequestDTO;
import com.FinalProject.TodoApp.dto.response.ProjectResponseDTO;
import com.FinalProject.TodoApp.entity.Project;
import com.FinalProject.TodoApp.entity.User;
import com.FinalProject.TodoApp.exception.DataNotFoundException;
import com.FinalProject.TodoApp.repository.ProjectRepository;
import com.FinalProject.TodoApp.repository.SubTaskRepository;
import com.FinalProject.TodoApp.repository.UserRepository;
import com.FinalProject.TodoApp.repository.TaskRepository;  // Thêm import cho TaskRepository
import com.FinalProject.TodoApp.service.IProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {

    private final ProjectRepository projectRepository;
    private final SubTaskRepository subTaskRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;  // Inject TaskRepository
    private final ModelMapper modelMapper;

    @Override
    public List<ProjectResponseDTO> getAllProjects(Integer userId) {
        List<Project> projects = projectRepository.findByUserId(userId);

        // Chuyển các project thành ProjectResponseDTO và thêm thông tin về số task
        return projects.stream()
                .map(this::mapProjectToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponseDTO getProjectById(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + projectId));

        return mapProjectToResponseDTO(project);
    }

    private ProjectResponseDTO mapProjectToResponseDTO(Project project) {
        // Lấy số lượng task và task hoàn thành cho mỗi project
        int totalTasks = taskRepository.countTotalTasksByProjectId(project.getId());
        int completedTasks = taskRepository.countCompletedTasksByProjectId(project.getId());

        // Sử dụng modelMapper để ánh xạ thông tin cơ bản từ Project sang ProjectResponseDTO
        ProjectResponseDTO dto = modelMapper.map(project, ProjectResponseDTO.class);

        // Cập nhật thêm số lượng task và task đã hoàn thành vào DTO
        dto.setTotalTasks(totalTasks);
        dto.setCompletedTasks(completedTasks);

        return dto;
    }

    @Override
    public ProjectResponseDTO createProject(ProjectRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User không tồn tại với ID: " + dto.getUserId()));

        Project project = Project.builder()
                .name(dto.getName())
                .color(dto.getColor())
                .user(user)
                .build();

        Project saved = projectRepository.save(project);
        return mapProjectToResponseDTO(saved);
    }

    @Override
    public ProjectResponseDTO updateProject(Integer projectId, ProjectRequestDTO dto) {
        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + projectId));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: " + dto.getUserId()));

        existingProject.setName(dto.getName());
        existingProject.setColor(dto.getColor());
        existingProject.setUser(user);

        Project updatedProject = projectRepository.save(existingProject);
        return mapProjectToResponseDTO(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Integer projectId) {
        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + projectId));

        // Xoá tất cả SubTask liên quan đến các Task trong Project
        taskRepository.findByProjectId(projectId).forEach(task -> {
            subTaskRepository.deleteByTaskId(task.getId());  // Xoá các SubTask của Task
        });

        // Xoá tất cả Task thuộc Project
        taskRepository.deleteByProjectId(projectId);  // Xoá tất cả Task thuộc Project

        // Cuối cùng, xoá Project
        projectRepository.delete(existingProject);
    }

}
