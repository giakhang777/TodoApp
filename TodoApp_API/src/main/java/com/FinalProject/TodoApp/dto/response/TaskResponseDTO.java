package com.FinalProject.TodoApp.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponseDTO extends BaseResponseDTO {
    private Integer id;
    private String title;
    private String priority;
    private LocalDate dueDate;
    private String project;
    private String label;
    private String description;
    private Boolean reminder;
    private LocalDateTime reminderTime;
    private Boolean completed;
    private List<SubResponseDTO> subTasks;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    private int totalSubTasks;
    private int completedSubTasks;
}
