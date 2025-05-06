package com.FinalProject.TodoApp.service.impl;

import com.FinalProject.TodoApp.dto.request.LabelRequestDTO;
import com.FinalProject.TodoApp.dto.response.LabelResponseDTO;
import com.FinalProject.TodoApp.entity.Label;
import com.FinalProject.TodoApp.exception.DataNotFoundException;
import com.FinalProject.TodoApp.repository.LabelRepository;
import com.FinalProject.TodoApp.service.ILabelService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LabelService implements ILabelService {
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public LabelResponseDTO createLabel(LabelRequestDTO dto) {
        Label label = modelMapper.map(dto, Label.class);
        label = labelRepository.save(label);
        return modelMapper.map(label, LabelResponseDTO.class);
    }

    @Override
    public List<LabelResponseDTO> getAllLabels() {
        return labelRepository.findAll().stream()
                .map(label -> modelMapper.map(label, LabelResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public LabelResponseDTO getLabelById(Integer id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Label not found with ID: " + id));
        return modelMapper.map(label, LabelResponseDTO.class);
    }

    @Override
    public LabelResponseDTO updateLabel(Integer id, LabelRequestDTO dto) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Label not found with ID: " + id));
        label.setTitle(dto.getTitle());
        return modelMapper.map(labelRepository.save(label), LabelResponseDTO.class);
    }

    @Override
    public void deleteLabel(Integer id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Label not found with ID: " + id));
        labelRepository.delete(label);
    }
}
