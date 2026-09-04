package com.example.demo.service;

import com.example.demo.dto.user.UserDTO;
import jakarta.mail.MessagingException;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    UserDTO getUserById(Long id);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void sendResetPasswordEmail(String email, String lang) throws MessagingException;

    void resetPassword(String token, String newPassword);

    void unbanUser(Long id);
}