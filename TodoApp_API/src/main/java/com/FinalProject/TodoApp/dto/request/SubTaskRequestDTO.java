package com.FinalProject.TodoApp.dto.request;

import com.FinalProject.TodoApp.entity.Task;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubTaskRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @JsonProperty("task_id")
    private Integer task;
}
