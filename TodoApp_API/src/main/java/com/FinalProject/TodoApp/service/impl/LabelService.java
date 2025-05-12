package com.FinalProject.TodoApp.service.impl;

import com.FinalProject.TodoApp.dto.request.LabelRequestDTO;
import com.FinalProject.TodoApp.dto.response.LabelResponseDTO;
import com.FinalProject.TodoApp.entity.Label;
import com.FinalProject.TodoApp.entity.User;
import com.FinalProject.TodoApp.exception.DataNotFoundException;
import com.FinalProject.TodoApp.repository.LabelRepository;
import com.FinalProject.TodoApp.repository.TaskRepository;
import com.FinalProject.TodoApp.repository.UserRepository;
import com.FinalProject.TodoApp.service.ILabelService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityExistsException;
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
    private TaskRepository taskRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public LabelResponseDTO createLabel(LabelRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: " + dto.getUserId()));

        // Kiểm tra xem label với title này đã tồn tại cho user chưa
        if (labelRepository.existsByUserIdAndTitle(dto.getUserId(), dto.getTitle())) {
            throw new EntityExistsException("Label with title '" + dto.getTitle() + "' already exists for this user.");
        }

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

        // Kiểm tra nếu title mới trùng với label khác (không phải chính nó)
        if (!label.getTitle().equals(dto.getTitle()) &&
                labelRepository.existsByUserIdAndTitle(dto.getUserId(), dto.getTitle())) {
            throw new EntityExistsException("Label with title '" + dto.getTitle() + "' already exists for this user.");
        }

        // Nếu cho phép cập nhật user
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with ID: " + dto.getUserId()));

        label.setTitle(dto.getTitle());
        label.setUser(user);

        label = labelRepository.save(label);
        return modelMapper.map(label, LabelResponseDTO.class);
    }
    @Transactional
    @Override
    public void deleteLabel(Integer id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Label not found with ID: " + id));

        // Cập nhật label_id của các Task có label tương ứng về null
        taskRepository.updateLabelIdToNull(id);

        // Sau đó, xóa label
        labelRepository.delete(label);
    }

}