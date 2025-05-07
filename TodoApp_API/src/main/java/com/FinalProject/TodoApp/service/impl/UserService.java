package com.FinalProject.TodoApp.service.impl;

import com.FinalProject.TodoApp.dto.request.UserUpdateRequestDTO;
import com.FinalProject.TodoApp.entity.User;
import com.FinalProject.TodoApp.repository.UserRepository;
import com.FinalProject.TodoApp.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean authenticate(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        return userOptional
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }
    public void saveUser(User user){
        userRepository.save(user);
    }

    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUser(Integer id, UserUpdateRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tùy chỉnh: chỉ map khi field không null
        if (dto.getUsername() != null) user.setUsername(dto.getUsername());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());

        return userRepository.save(user);
    }
}
