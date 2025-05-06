package com.FinalProject.TodoApp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabelRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;
}
