package com.FinalProject.TodoApp.service.impl;

import com.FinalProject.TodoApp.dto.request.LabelRequestDTO;
import com.FinalProject.TodoApp.dto.response.LabelResponseDTO;
import com.FinalProject.TodoApp.entity.Label;
import com.FinalProject.TodoApp.entity.User;
import com.FinalProject.TodoApp.exception.DataNotFoundException;
import com.FinalProject.TodoApp.repository.LabelRepository;
import com.FinalProject.TodoApp.repository.UserRepository;
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
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public LabelResponseDTO createLabel(LabelRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: " + dto.getUserId()));

        Label label = Label.builder()
                .title(dto.getTitle())
                .user(user)
                .build();

        label = labelRepository.save(label);
        return modelMapper.map(label, LabelResponseDTO.class);
    }

    @Override
    public List<LabelResponseDTO> getAllLabels(Integer userId) {
        List<Label> labels = labelRepository.findByUserId(userId);
        return labels.stream()
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

        // Nếu cho phép cập nhật user:
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: " + dto.getUserId()));

        label.setTitle(dto.getTitle());
        label.setUser(user);

        return modelMapper.map(labelRepository.save(label), LabelResponseDTO.class);
    }

    @Override
    public void deleteLabel(Integer id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Label not found with ID: " + id));
        labelRepository.delete(label);
    }
}
