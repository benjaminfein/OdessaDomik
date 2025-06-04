package com.example.demo.service.impl;

import com.example.demo.dto.user.UserDTO;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.User;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImpl emailServiceImpl;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream().map(UserMapper::mapToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с таким id не найден"));
        return UserMapper.mapToUserDTO(user);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с таким id не найден"));

        if (userRepository.existsByEmail(userDTO.getEmail()) && !userDTO.getEmail().equals(userToUpdate.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        if (userRepository.existsByUsername(userDTO.getUsername()) && !userDTO.getUsername().equals(userToUpdate.getUsername())) {
            throw new RuntimeException("Пользователь с таким username уже существует");
        }

        userToUpdate.setUsername(userDTO.getUsername());
        userToUpdate.setEmail(userDTO.getEmail());
        userToUpdate.setPhoneNumber(userDTO.getPhoneNumber());
        userToUpdate.setName(userDTO.getName());

        userRepository.save(userToUpdate);
        return UserMapper.mapToUserDTO(userToUpdate);
    }

    @Override
    @Transactional
    public void sendResetPasswordEmail(String email, String lang) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с таким email не найден"));

        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                null,
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(24),
                user
        );
        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/" + lang + "/reset-password?token=" + token;

        emailServiceImpl.sendEmail(
                user.getEmail(),
                "reset_password",
                Map.of(
                        "link", resetLink,
                        "username", user.getUsername(),
                        "lang", lang
                )
        );
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Неверный токен"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Токен истек");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }
}