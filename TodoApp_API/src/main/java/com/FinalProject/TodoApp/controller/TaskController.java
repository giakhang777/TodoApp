package com.FinalProject.TodoApp.controller;

import com.FinalProject.TodoApp.dto.request.TaskRequestDTO;
import com.FinalProject.TodoApp.dto.response.TaskResponseDTO;
import com.FinalProject.TodoApp.service.ITaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/task")
public class TaskController {
    @Autowired
    private ITaskService taskService;

    @PostMapping("")
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskRequestDTO dto) {
        try {
            TaskResponseDTO task = taskService.createTask(dto);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@Valid @PathVariable Integer id) {
        try {
            TaskResponseDTO task = taskService.getTaskById(id);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getTasksByProject(@Valid @PathVariable Integer projectId) {
        try {
            List<TaskResponseDTO> tasks = taskService.getTasksByProjectId(projectId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTasksByUser(@Valid @PathVariable Integer userId) {
        try {
            List<TaskResponseDTO> tasks = taskService.getTasksByUserId(userId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@Valid @PathVariable Integer id, @Valid @RequestBody TaskRequestDTO dto) {
        try {
            TaskResponseDTO updated = taskService.updateTask(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@Valid @PathVariable Integer id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok("Task deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<?> changeTaskStatus(@PathVariable Integer taskId,
                                              @RequestBody Boolean completed) {
        try {
            TaskResponseDTO updated = taskService.changeTaskStatus(taskId, completed);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/date/{date}/{userId}")
    public ResponseEntity<?> getTasksByDate(@Valid @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @Valid @PathVariable Integer userId) {
        try {
            List<TaskResponseDTO> tasks = taskService.getAllByDate(date,userId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
