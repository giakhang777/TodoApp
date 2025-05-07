package com.FinalProject.TodoApp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabelRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;
    @JsonProperty("user_id")
    @Min(value = 1, message = "User's ID must be > 0")
    private Integer userId;
}
