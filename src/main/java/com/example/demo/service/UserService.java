package com.example.demo.service;

import com.example.demo.dto.user.CreateUserDTO;
import com.example.demo.dto.user.UserDTO;

public interface UserService {

    CreateUserDTO createUser(UserDTO userDTO);
}
