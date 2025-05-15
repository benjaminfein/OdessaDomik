package com.example.demo.mapper;

import com.example.demo.dto.user.CreateUserDTO;
import com.example.demo.dto.user.UserDTO;
import com.example.demo.model.User;

public class UserMapper {
    public static UserDTO mapToUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getName(),
                user.getPassword(),
                String.valueOf(user.getEmailConfirmed())
        );
    }

    public static User mapToUser(UserDTO userDTO) {
        return new User(
                userDTO.getId(),
                userDTO.getUsername(),
                userDTO.getEmail(),
                userDTO.getPhoneNumber(),
                userDTO.getRole(),
                userDTO.getName(),
                userDTO.getPassword()
        );
    }

    public static CreateUserDTO mapToCreateUserDTO(User user) {
        return new CreateUserDTO(
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getName(),
                user.getPassword()
        );
    }

    public static User mapToUser(CreateUserDTO createUserDTO) {
        return new User(
                createUserDTO.getUsername(),
                createUserDTO.getEmail(),
                createUserDTO.getPhoneNumber(),
                createUserDTO.getRole(),
                createUserDTO.getName(),
                createUserDTO.getPassword()
        );
    }
}
