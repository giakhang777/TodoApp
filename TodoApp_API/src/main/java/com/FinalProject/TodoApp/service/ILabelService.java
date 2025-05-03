package com.FinalProject.TodoApp.service;

import com.FinalProject.TodoApp.dto.request.LabelRequestDTO;
import com.FinalProject.TodoApp.dto.response.LabelResponseDTO;

import java.util.List;

public interface ILabelService {
    LabelResponseDTO createLabel(LabelRequestDTO dto);
    List<LabelResponseDTO> getAllLabels();
    LabelResponseDTO getLabelById(Integer id);
    LabelResponseDTO updateLabel(Integer id, LabelRequestDTO dto);
    void deleteLabel(Integer id);
}
