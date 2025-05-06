package com.FinalProject.TodoApp.dto.response;

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
public class TaskResponseDTO extends BaseResponseDTO {
    private Integer id;
    private String title;
    private String priority;
    private LocalDate dueDate;
    private String label;
    private String description;
    private Boolean reminder;
    private LocalDateTime reminderTime;
    private Boolean completed;
}
