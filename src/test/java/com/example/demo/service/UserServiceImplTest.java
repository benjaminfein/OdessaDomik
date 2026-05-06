package com.example.demo.service;

import com.example.demo.dto.user.UserDTO;
import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.User;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.EmailServiceImpl;
import com.example.demo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailServiceImpl emailServiceImpl;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "frontendUrl", "http://localhost:3000");
    }

    // --- getAllUsers ---

    @Test
    void getAllUsers_ShouldReturnMappedDTOs() {
        User u1 = new User(1L, "alice", "alice@test.com", null, "client", "Alice", "pwd");
        User u2 = new User(2L, "bob", "bob@test.com", null, "admin", "Bob", "pwd");
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("alice", result.get(0).getUsername());
        assertEquals("bob", result.get(1).getUsername());
    }

    // --- getUserById ---

    @Test
    void getUserById_ShouldReturnDTO_WhenFound() {
        User user = new User(1L, "alice", "alice@test.com", null, "client", "Alice", "pwd");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertEquals("alice", result.getUsername());
        assertEquals("alice@test.com", result.getEmail());
    }

    @Test
    void getUserById_ShouldThrow_WhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(99L));
    }

    // --- updateUser ---

    @Test
    void updateUser_ShouldUpdateAndReturnDTO() {
        User existing = new User(1L, "alice", "alice@test.com", null, "client", "Alice", "pwd");
        UserDTO dto = new UserDTO();
        dto.setUsername("alice2");
        dto.setEmail("alice@test.com");
        dto.setName("Alice Updated");
        dto.setPhoneNumber("+380991234567");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);
        when(userRepository.existsByUsername("alice2")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(existing);

        UserDTO result = userService.updateUser(1L, dto);

        assertEquals("alice2", result.getUsername());
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_ShouldThrow_WhenEmailAlreadyTaken() {
        User existing = new User(1L, "alice", "alice@test.com", null, "client", "Alice", "pwd");
        UserDTO dto = new UserDTO();
        dto.setUsername("alice");
        dto.setEmail("taken@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldThrow_WhenUsernameAlreadyTaken() {
        User existing = new User(1L, "alice", "alice@test.com", null, "client", "Alice", "pwd");
        UserDTO dto = new UserDTO();
        dto.setUsername("takenUser");
        dto.setEmail("alice@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);
        when(userRepository.existsByUsername("takenUser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, dto));
        verify(userRepository, never()).save(any());
    }

    // --- sendResetPasswordEmail ---

    @Test
    void sendResetPasswordEmail_ShouldSendEmail_WhenUserExists() throws Exception {
        User user = new User(1L, "alice", "alice@test.com", null, "client", "Alice", "pwd");
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

        userService.sendResetPasswordEmail("alice@test.com", "ua");

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailServiceImpl).sendEmail(eq("alice@test.com"), eq("reset_password"), anyMap());
    }

    @Test
    void sendResetPasswordEmail_ShouldThrow_WhenUserNotFound() throws Exception {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.sendResetPasswordEmail("nobody@test.com", "ua"));
        verify(emailServiceImpl, never()).sendEmail(any(), any(), any());
    }

    // --- resetPassword ---

    @Test
    void resetPassword_ShouldEncodeAndSavePassword_WhenTokenValid() {
        User user = new User(1L, "alice", "alice@test.com", null, "client", "Alice", "pwd");
        PasswordResetToken token = new PasswordResetToken(1L, "valid-token",
                LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusHours(1), user);

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedPassword");

        userService.resetPassword("valid-token", "newPassword123");

        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(token);
        assertEquals("encodedPassword", user.getPassword());
    }

    @Test
    void resetPassword_ShouldThrow_WhenTokenNotFound() {
        when(passwordResetTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.resetPassword("bad-token", "newPassword123"));
    }

    @Test
    void resetPassword_ShouldThrow_WhenTokenExpired() {
        User user = new User(1L, "alice", "alice@test.com", null, "client", "Alice", "pwd");
        PasswordResetToken token = new PasswordResetToken(1L, "expired-token",
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), user);

        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThrows(RuntimeException.class,
                () -> userService.resetPassword("expired-token", "newPassword123"));
        verify(userRepository, never()).save(any());
    }
}
