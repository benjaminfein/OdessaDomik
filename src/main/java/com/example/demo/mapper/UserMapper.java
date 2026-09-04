package com.example.demo.mapper;

import com.example.demo.dto.user.UserDTO;
import com.example.demo.model.User;

public class UserMapper {
    public static UserDTO mapToUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getName(),
                user.getPassword(),
                String.valueOf(user.getEmailConfirmed()),
                user.getBannedUntil() != null ? user.getBannedUntil().toString() : null
        );
    }
}
