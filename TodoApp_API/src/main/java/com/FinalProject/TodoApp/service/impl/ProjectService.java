package com.FinalProject.TodoApp.service.impl;

import com.FinalProject.TodoApp.dto.request.ProjectRequestDTO;
import com.FinalProject.TodoApp.dto.response.ProjectResponseDTO;
import com.FinalProject.TodoApp.dto.response.TaskResponseDTO;
import com.FinalProject.TodoApp.entity.Project;
import com.FinalProject.TodoApp.entity.User;
import com.FinalProject.TodoApp.exception.DataNotFoundException;
import com.FinalProject.TodoApp.repository.ProjectRepository;
import com.FinalProject.TodoApp.repository.UserRepository;
import com.FinalProject.TodoApp.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

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

        if (project.getTasks() != null) {
            List<TaskResponseDTO> taskDTOs = project.getTasks().stream()
                    .map(task -> modelMapper.map(task, TaskResponseDTO.class))
                    .collect(Collectors.toList());

            dto.setTasks(taskDTOs);
            dto.setTaskCount(taskDTOs.size());
        } else {
            dto.setTaskCount(0);
        }

        return dto;
    }

    @Override
    public ProjectResponseDTO createProject(ProjectRequestDTO projectRequest) {
        User user = userRepository.findById(projectRequest.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: " + projectRequest.getUserId()));

        modelMapper.typeMap(ProjectRequestDTO.class, Project.class)
                .addMappings(mapper -> mapper.skip(Project::setId));

        Project project = new Project();
        modelMapper.map(projectRequest, project);
        project.setUser(user);

        Project savedProject = projectRepository.save(project);

        return modelMapper.map(savedProject, ProjectResponseDTO.class);
    }


    @Override
    public ProjectResponseDTO updateProject(Integer projectId, ProjectRequestDTO projectRequest) {
        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + projectId));

        existingProject.setName(projectRequest.getName());
        existingProject.setColor(projectRequest.getColor());
        // Update other fields as needed

        Project updatedProject = projectRepository.save(existingProject);

        return modelMapper.map(updatedProject, ProjectResponseDTO.class);
    }


    @Override
    public void deleteProject(Integer projectId) {
        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("Project not found with ID: " + projectId));
        projectRepository.delete(existingProject);
    }

}
