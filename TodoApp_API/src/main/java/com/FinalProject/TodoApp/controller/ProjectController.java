package com.FinalProject.TodoApp.controller;

import com.FinalProject.TodoApp.dto.request.ProjectRequestDTO;
import com.FinalProject.TodoApp.dto.response.ProjectResponseDTO;
import com.FinalProject.TodoApp.entity.Project;
import com.FinalProject.TodoApp.service.IProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
public class ProjectController {
    @Autowired
    private IProjectService projectService;

    // Lấy tất cả các project của user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllProjects(@Valid @PathVariable Integer userId) {
        try {
            List<ProjectResponseDTO> projects = projectService.getAllProjects(userId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lấy project theo ID
    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProjectById(@Valid @PathVariable Integer projectId) {
        try {
            ProjectResponseDTO project = projectService.getProjectById(projectId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Thêm mới project
    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody ProjectRequestDTO projectRequest) {
        try {
            ProjectResponseDTO createdProject = projectService.createProject(projectRequest);
            return ResponseEntity.ok(createdProject);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Cập nhật project
    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(@Valid @PathVariable Integer projectId,@Valid @RequestBody ProjectRequestDTO projectRequest) {
        try {
            ProjectResponseDTO updatedProject = projectService.updateProject(projectId, projectRequest);
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa project
    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@Valid @PathVariable Integer projectId) {
        try {
            projectService.deleteProject(projectId);
            return ResponseEntity.ok("Project deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}