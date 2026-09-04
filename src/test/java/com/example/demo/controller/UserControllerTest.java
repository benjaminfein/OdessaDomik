package com.example.demo.controller;

import com.example.demo.dto.user.CreateUserDTO;
import com.example.demo.dto.user.LoginDTO;
import com.example.demo.dto.user.UserDTO;
import com.example.demo.model.ConfirmationToken;
import com.example.demo.model.User;
import com.example.demo.repository.ConfirmationTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserRepository userRepository;
    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private UserService userService;
    @MockBean private ConfirmationTokenRepository confirmationTokenRepository;
    @MockBean private EmailService emailService;
    @MockBean private JavaMailSenderImpl javaMailSender;

    private User confirmedUser() {
        User u = new User(1L, "alice@test.com", "+380991234567", "client", "Alice", "encodedPwd");
        u.setEmailConfirmed(true);
        u.setDateOfCreated(new Date());
        return u;
    }

    @Test
    void login_ShouldReturn200WithToken_WhenCredentialsValid() throws Exception {
        User user = confirmedUser();
        LoginDTO loginDTO = new LoginDTO("alice@test.com", "password123");

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("alice@test.com", null, List.of()));
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void login_ShouldReturn400_WhenCredentialsInvalid() throws Exception {
        LoginDTO loginDTO = new LoginDTO("alice@test.com", "wrongpass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturn403_WhenEmailNotConfirmed() throws Exception {
        User user = confirmedUser();
        user.setEmailConfirmed(false);
        LoginDTO loginDTO = new LoginDTO("alice@test.com", "password123");

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("alice@test.com", null, List.of()));
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_ShouldReturn400_WhenEmailMissing() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"pass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldReturn200_WhenValidData() throws Exception {
        CreateUserDTO dto = new CreateUserDTO("alice@test.com", "Alice",
                "+380991234567", "client", "password123", "ua");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(confirmedUser());
        when(confirmationTokenRepository.save(any())).thenReturn(mock(ConfirmationToken.class));

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Confirmation email sent"));
    }

    @Test
    void register_ShouldReturn400_WhenEmailAlreadyExists() throws Exception {
        CreateUserDTO dto = new CreateUserDTO("taken@test.com", "Alice",
                null, null, "password123", "ua");

        when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(confirmedUser()));

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email already exists"));
    }

    @Test
    void register_ShouldReturn400_WhenPasswordTooShort() throws Exception {
        CreateUserDTO dto = new CreateUserDTO("alice@test.com", "Alice",
                null, null, "pwd", "ua");

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProfile_ShouldReturn200_WhenAuthenticated() throws Exception {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(confirmedUser()));

        mockMvc.perform(get("/api/user/profile")
                        .with(jwt().jwt(j -> j.subject("alice@test.com"))
                                .authorities(new SimpleGrantedAuthority("client"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Email").value("alice@test.com"));
    }

    @Test
    void getProfile_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllUsers_ShouldReturn200_WhenAdmin() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("alice@test.com");
        when(userService.getAllUsers()).thenReturn(List.of(userDTO));

        mockMvc.perform(get("/api/user/allUsers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("alice@test.com"));
    }

    @Test
    void getAllUsers_ShouldReturn200_WhenClientRole_OnDev() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/user/allUsers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("client"))))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_ShouldReturn200_WhenAdmin() throws Exception {
        doNothing().when(confirmationTokenRepository).deleteByUserId(1L);
        doNothing().when(userRepository).deleteById(1L);

        mockMvc.perform(delete("/api/user/delete/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully!"));
    }

    @Test
    void getUser_ShouldReturn200_WhenAuthenticated() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("alice@test.com");
        when(userService.getUserById(1L)).thenReturn(userDTO);

        mockMvc.perform(get("/api/user/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("client"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@test.com"));
    }

    @Test
    void getUser_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateUser_ShouldReturn200_WhenAuthenticated() throws Exception {
        UserDTO requestDto = new UserDTO();
        requestDto.setName("Alice Updated");

        UserDTO updatedDto = new UserDTO();
        updatedDto.setId(1L);
        updatedDto.setEmail("alice@test.com");
        updatedDto.setName("Alice Updated");
        when(userService.updateUser(eq(1L), any())).thenReturn(updatedDto);

        mockMvc.perform(put("/api/user/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("client")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"));
    }

    @Test
    void unbanUser_ShouldReturn200_WhenAdmin() throws Exception {
        doNothing().when(userService).unbanUser(1L);

        mockMvc.perform(put("/api/user/1/unban")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(content().string("User unbanned successfully!"));
    }

    @Test
    void confirmEmail_ShouldReturn200_WhenTokenValid() throws Exception {
        User user = confirmedUser();
        user.setEmailConfirmed(false);
        ConfirmationToken token = mock(ConfirmationToken.class);
        when(token.getExpiresAt()).thenReturn(java.time.LocalDateTime.now().plusHours(1));
        when(token.getUser()).thenReturn(user);
        when(confirmationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        mockMvc.perform(get("/api/user/confirm").param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email confirmed successfully"));
    }

    @Test
    void forgotPassword_ShouldReturn200() throws Exception {
        doNothing().when(userService).sendResetPasswordEmail(any(), any());

        mockMvc.perform(post("/api/user/forgot-password")
                        .param("email", "alice@test.com")
                        .param("lang", "ua"))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_ShouldReturn200() throws Exception {
        doNothing().when(userService).resetPassword(any(), any());

        mockMvc.perform(post("/api/user/reset-password")
                        .param("token", "some-token")
                        .param("newPassword", "newPassword123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Password successfully changed."));
    }
}
