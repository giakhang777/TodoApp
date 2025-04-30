package com.FinalProject.TodoApp.service;

import com.FinalProject.TodoApp.dto.request.ProjectRequestDTO;
import com.FinalProject.TodoApp.dto.response.ProjectResponseDTO;
import com.FinalProject.TodoApp.entity.Project;

import java.util.List;

public interface IProjectService {
    List<ProjectResponseDTO> getAllProjects(Integer userId);
    ProjectResponseDTO getProjectById(Integer projectId);

    ProjectResponseDTO createProject(ProjectRequestDTO projectRequest) throws Exception;
    ProjectResponseDTO updateProject(Integer projectId, ProjectRequestDTO projectRequest);
    void deleteProject(Integer projectId);
}
