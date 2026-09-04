package com.example.demo.service.impl;

import com.example.demo.dto.user.UserDTO;
import com.example.demo.exception.DuplicateUserException;
import com.example.demo.exception.EmailDeliveryException;
import com.example.demo.exception.InvalidOrExpiredTokenException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.User;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImpl emailServiceImpl;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${frontend_url}")
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
                .orElseThrow(() -> new UserNotFoundException("User not found with given id"));
        return UserMapper.mapToUserDTO(user);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with given id"));

        if (userRepository.existsByEmail(userDTO.getEmail()) && !userDTO.getEmail().equals(userToUpdate.getEmail())) {
            throw new DuplicateUserException("User with this email already exists", "email");
        }

        userToUpdate.setEmail(userDTO.getEmail());
        userToUpdate.setPhoneNumber(userDTO.getPhoneNumber());
        userToUpdate.setName(userDTO.getName());

        userRepository.save(userToUpdate);
        return UserMapper.mapToUserDTO(userToUpdate);
    }

    @Override
    @Transactional
    public void sendResetPasswordEmail(String email, String lang) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with given email"));

        passwordResetTokenRepository.deletePasswordResetTokenByUserIdByUserId(user.getId());

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

        try {
            emailServiceImpl.sendEmail(
                    user.getEmail(),
                    "reset_password",
                    Map.of(
                            "link", resetLink,
                            "username", user.getName(),
                            "lang", lang
                    )
            );
            log.info("[UserServiceImpl] Reset email sent to {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("[UserServiceImpl] Failed to send reset email to {}", user.getEmail(), e);
            throw new EmailDeliveryException("Failed to send password reset email", e);
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Invalid token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidOrExpiredTokenException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }

    @Override
    public void unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with given id"));

        user.setBannedUntil(null);
        user.setBanStrikeCount(0);
        user.setBanWindowStart(null);
        userRepository.save(user);
    }
}