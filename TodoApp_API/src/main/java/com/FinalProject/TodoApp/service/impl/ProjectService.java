package com.FinalProject.TodoApp.service.impl;

import com.FinalProject.TodoApp.dto.request.ProjectRequestDTO;
import com.FinalProject.TodoApp.dto.response.ProjectResponseDTO;
import com.FinalProject.TodoApp.entity.Project;
import com.FinalProject.TodoApp.entity.User;
import com.FinalProject.TodoApp.exception.DataNotFoundException;
import com.FinalProject.TodoApp.repository.ProjectRepository;
import com.FinalProject.TodoApp.repository.UserRepository;
import com.FinalProject.TodoApp.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ProjectResponseDTO> getAllProjects(Integer userId) {
        List<Project> projects = projectRepository.findByUserId(userId);

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
        ProjectResponseDTO dto = modelMapper.map(project, ProjectResponseDTO.class);

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
    public void deleteProject(Integer projectId) {
        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + projectId));
        projectRepository.delete(existingProject);
    }
}
