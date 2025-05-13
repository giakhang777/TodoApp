package com.FinalProject.TodoApp.dto.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponseDTO extends BaseResponseDTO {
    private Integer id;
    private String name;
    private String color;
    private int totalTasks;      // Thêm số lượng task
    private int completedTasks;
}
