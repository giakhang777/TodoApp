package com.FinalProject.TodoApp.controller;

import com.FinalProject.TodoApp.dto.request.UserLoginRequestDTO;
import com.FinalProject.TodoApp.dto.request.UserUpdateRequestDTO;
import com.FinalProject.TodoApp.dto.response.UserResponseDTO;
import com.FinalProject.TodoApp.entity.User;
import com.FinalProject.TodoApp.service.IUserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDTO requestDTO) {
        // Lấy mật khẩu từ DTO (chưa mã hóa)
        String rawPassword = requestDTO.getPassword();

        // Tìm người dùng từ cơ sở dữ liệu
        Optional<User> userOptional = userService.findByUsername(requestDTO.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // So sánh mật khẩu chưa mã hóa với mật khẩu đã mã hóa trong cơ sở dữ liệu
            if (userService.authenticate(requestDTO.getUsername(), rawPassword)) {
                // Nếu thành công, ánh xạ từ entity sang DTO để trả về
                UserResponseDTO responseDTO = modelMapper.map(user, UserResponseDTO.class);
                return ResponseEntity.ok(responseDTO);

            }
        }

        return ResponseEntity.status(401).body(Map.of("message", "Invalid username or password"));
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody UserUpdateRequestDTO updateDTO) {
        User updatedUser = userService.updateUser(id, updateDTO);
        UserResponseDTO responseDTO = modelMapper.map(updatedUser, UserResponseDTO.class);
        return ResponseEntity.ok(responseDTO);
    }


}
