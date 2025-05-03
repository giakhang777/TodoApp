package com.FinalProject.TodoApp.controller;

import com.FinalProject.TodoApp.dto.request.LabelRequestDTO;
import com.FinalProject.TodoApp.dto.response.LabelResponseDTO;
import com.FinalProject.TodoApp.service.ILabelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/label")
public class LabelController {
    @Autowired
    private ILabelService labelService;

    @PostMapping("")
    public ResponseEntity<?> createLabel(@Valid @RequestBody LabelRequestDTO dto) {
        try {
            return ResponseEntity.ok(labelService.createLabel(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getAllLabels() {
        try {
            List<LabelResponseDTO> labels = labelService.getAllLabels();
            return ResponseEntity.ok(labels);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLabelById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(labelService.getLabelById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLabel(@PathVariable Integer id, @Valid @RequestBody LabelRequestDTO dto) {
        try {
            return ResponseEntity.ok(labelService.updateLabel(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLabel(@PathVariable Integer id) {
        try {
            labelService.deleteLabel(id);
            return ResponseEntity.ok("Label deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
