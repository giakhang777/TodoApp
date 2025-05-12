package com.FinalProject.TodoApp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequestDTO {

    @JsonProperty("user_id")
    @Min(value = 1, message = "User ID must be greater than 0")
    private Integer userId;

    @NotBlank(message = "Project name is required")
    private String name;

    @NotBlank(message = "Color is required")
    private String color;
}
