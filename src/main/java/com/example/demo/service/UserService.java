package com.example.demo.service;

import com.example.demo.dto.user.UserDTO;

import java.util.List;

public interface UserService {

    List<UserDTO> getAllUsers();
}
