package com.FinalProject.TodoApp.service.impl;

import com.FinalProject.TodoApp.dto.request.ProjectRequestDTO;
import com.FinalProject.TodoApp.dto.response.ProjectResponseDTO;
import com.FinalProject.TodoApp.dto.response.TaskResponseDTO;
import com.FinalProject.TodoApp.entity.Label;
import com.FinalProject.TodoApp.entity.Project;
import com.FinalProject.TodoApp.entity.User;
import com.FinalProject.TodoApp.exception.DataNotFoundException;
import com.FinalProject.TodoApp.repository.LabelRepository;
import com.FinalProject.TodoApp.repository.ProjectRepository;
import com.FinalProject.TodoApp.repository.UserRepository;
import com.FinalProject.TodoApp.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ModelMapper modelMapper;

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

        // Gán tên label nếu có
        dto.setLabel(project.getLabel() != null ? project.getLabel().getTitle() : null);

        // Không còn tasks trong Project nên không set taskCount hoặc danh sách task
        dto.setTaskCount(0);  // hoặc để null nếu muốn
        dto.setTasks(null);

        return dto;
    }

    @Override
    public ProjectResponseDTO createProject(ProjectRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User không tồn tại với ID: " + dto.getUserId()));

        Label label = null;
        if (dto.getLabelId() != null) {
            label = labelRepository.findById(dto.getLabelId())
                    .orElseThrow(() -> new DataNotFoundException("Label không tồn tại với ID: " + dto.getLabelId()));
        }

        Project project = Project.builder()
                .name(dto.getName())
                .color(dto.getColor())
                .user(user)
                .label(label)
                .build();

        Project saved = projectRepository.save(project);
        return mapProjectToResponseDTO(saved);
    }

    @Override
    public ProjectResponseDTO updateProject(Integer projectId, ProjectRequestDTO projectRequest) {
        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + projectId));

        User user = userRepository.findById(projectRequest.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: " + projectRequest.getUserId()));

        Label label = null;
        if (projectRequest.getLabelId() != null) {
            label = labelRepository.findById(projectRequest.getLabelId())
                    .orElseThrow(() -> new DataNotFoundException("Label not found with ID: " + projectRequest.getLabelId()));
        }

        existingProject.setName(projectRequest.getName());
        existingProject.setColor(projectRequest.getColor());
        existingProject.setUser(user);
        existingProject.setLabel(label);

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


