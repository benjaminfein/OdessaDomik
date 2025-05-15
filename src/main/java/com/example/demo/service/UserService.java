package com.example.demo.service;

import com.example.demo.dto.user.ChangePasswordDTO;
import com.example.demo.dto.user.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    UserDTO getUserById(Long id);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void changePassword(Long userId, ChangePasswordDTO dto);
}