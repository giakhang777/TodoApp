package com.FinalProject.TodoApp.controller;

import com.FinalProject.TodoApp.dto.request.LabelRequestDTO;
import com.FinalProject.TodoApp.dto.response.LabelResponseDTO;
import com.FinalProject.TodoApp.exception.DataNotFoundException;
import com.FinalProject.TodoApp.service.impl.LabelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/label")
public class LabelController {

    @Autowired
    private LabelService labelService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllLabels(@Valid @PathVariable Integer userId) {
        try {
            List<LabelResponseDTO> labels = labelService.getAllLabels(userId);
            return ResponseEntity.ok(labels);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createLabel(@Valid @RequestBody LabelRequestDTO labelRequest) {
        try {
            LabelResponseDTO createdLabel = labelService.createLabel(labelRequest);
            return ResponseEntity.ok(createdLabel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{labelId}")
    public ResponseEntity<?> updateLabel(@Valid @PathVariable Integer labelId, @Valid @RequestBody LabelRequestDTO labelRequest) {
        try {
            LabelResponseDTO updatedLabel = labelService.updateLabel(labelId, labelRequest);
            return ResponseEntity.ok(updatedLabel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{labelId}")
    public ResponseEntity<?> deleteLabel(@Valid @PathVariable Integer labelId) {
        try {
            labelService.deleteLabel(labelId);
            return ResponseEntity.ok("Label deleted successfully");
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete label: " + e.getMessage());
        }
    }
}