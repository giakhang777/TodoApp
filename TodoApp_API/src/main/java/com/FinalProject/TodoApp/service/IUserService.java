package com.FinalProject.TodoApp.service;

import com.FinalProject.TodoApp.dto.request.UserUpdateRequestDTO;
import com.FinalProject.TodoApp.entity.User;

import java.util.Optional;


public interface IUserService {
    boolean authenticate(String username, String password);
    void saveUser(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    User updateUser(Integer id, UserUpdateRequestDTO dto);
}
