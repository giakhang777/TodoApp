package com.FinalProject.TodoApp.mapper;

import com.FinalProject.TodoApp.dto.request.ProjectRequestDTO;
import com.FinalProject.TodoApp.entity.Project;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // Bỏ qua thuộc tính user khi map từ DTO sang Entity
        mapper.typeMap(ProjectRequestDTO.class, Project.class)
                .addMappings(map -> map.skip(Project::setUser));

        return mapper;
    }
}

