package com.FinalProject.TodoApp.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubResponseDTO{
    private Integer id;
    private String title;
    private Boolean completed;
}
