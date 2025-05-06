package com.FinalProject.TodoApp.controller;

import com.FinalProject.TodoApp.dto.request.SubTaskRequestDTO;
import com.FinalProject.TodoApp.dto.response.SubResponseDTO;
import com.FinalProject.TodoApp.service.ISubTaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subtask")
public class SubTaskController {

    @Autowired
    private ISubTaskService subTaskService;

    @PostMapping("")
    public ResponseEntity<?> createSubTask(@Valid @RequestBody SubTaskRequestDTO dto) {
        try {
            SubResponseDTO response = subTaskService.createSubTask(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getSubTasksByTaskId(@Valid @PathVariable Integer taskId) {
        try {
            List<SubResponseDTO> subTasks = subTaskService.getSubTasksByTaskId(taskId);
            return ResponseEntity.ok(subTasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{subTaskId}")
    public ResponseEntity<?> updateSubTask(@Valid @PathVariable Integer subTaskId,
                                           @Valid @RequestBody SubTaskRequestDTO dto) {
        try {
            SubResponseDTO updated = subTaskService.updateSubTask(subTaskId, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{subTaskId}")
    public ResponseEntity<?> deleteSubTask(@Valid @PathVariable Integer subTaskId) {
        try {
            subTaskService.deleteSubTask(subTaskId);
            return ResponseEntity.ok("SubTask deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PatchMapping("/{subTaskId}/status")
    public ResponseEntity<?> changeSubTaskStatus(@PathVariable Integer subTaskId,
                                                 @RequestBody Boolean completed) {
        try {
            SubResponseDTO updated = subTaskService.changeSubTaskStatus(subTaskId, completed);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}

