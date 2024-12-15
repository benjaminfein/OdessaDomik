package com.example.demo.service.impl;

import com.example.demo.dto.user.CreateUserDTO;
import com.example.demo.dto.user.UserDTO;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.enums.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CreateUserDTO createUser(UserDTO userDTO) {
        User user = UserMapper.mapToUser(userDTO);
        String email = user.getEmail();
        if (userRepository.findByEmail(email) == null) {
            user.setActive(true);
            user.getRoles().add(Role.ROLE_USER);
            User savedUser = userRepository.save(user);
            return UserMapper.mapToCreateUserDTO(savedUser);
        } else {
            return null;
        }
    }
}
