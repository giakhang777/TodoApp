package com.FinalProject.TodoApp.dto.request;

import com.FinalProject.TodoApp.entity.Project;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequestDTO {
    private String title;
    private String priority;
    private LocalDate dueDate;
    private String label;
    private String description;
    private Boolean reminder;
    @JsonProperty("reminder_time")
    private LocalDateTime reminderTime;
    @JsonProperty("project_id")
    private Integer projectId;
}
