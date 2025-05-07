package com.FinalProject.TodoApp.dto.request;

import com.FinalProject.TodoApp.entity.Project;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Title is required")
    private String title;

    @JsonProperty("user_id")
    @Min(value = 1, message = "User's ID must be > 0")
    private Integer userId;

    private String priority;
    private LocalDate dueDate;

    private String description;
    private Boolean reminder;
    @JsonProperty("reminder_time")
    private LocalDateTime reminderTime;
    @JsonProperty("project_id")
    private Integer projectId;
    @JsonProperty("label_id")
    private Integer labelId;
}
