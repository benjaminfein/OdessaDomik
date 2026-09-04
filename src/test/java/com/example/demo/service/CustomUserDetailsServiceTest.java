package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenFound() {
        User user = new User(1L, "alice@test.com", null, "client", "Alice", "encodedPwd");
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("alice@test.com");

        assertEquals("alice@test.com", details.getUsername());
        assertEquals("encodedPwd", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_client")));
    }

    @Test
    void loadUserByUsername_ShouldThrow_WhenNotFound() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> customUserDetailsService.loadUserByUsername("nobody@test.com"));
    }
}
