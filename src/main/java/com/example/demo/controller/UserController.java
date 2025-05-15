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
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin("*")
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Value("${security.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    @Value("${frontend.url}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final EmailService emailService;

    @Autowired
    public UserController(UserRepository userRepository, AuthenticationManager authenticationManager,
                          UserService userService, ConfirmationTokenRepository confirmationTokenRepository,
                          EmailService emailService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.emailService = emailService;
    }

    @GetMapping("/profile")
    public ResponseEntity<Object> profile(Authentication auth) {
        var response = new HashMap<String, Object>();
        response.put("Email", auth.getName());
        response.put("Authorities", auth.getAuthorities());

        // Ищем пользователя в базе данных
        var user = userRepository.findByEmail(auth.getName());
        if (user == null) {
            // Возвращаем ошибку, если пользователь не найден
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Bearer not valid!");
        }

        // Если пользователь найден, добавляем его в ответ
        response.put("User", user);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody CreateUserDTO createUserDTO, BindingResult result, HttpServletResponse response) {
        if (result.hasErrors()) {
            var errorsList = result.getAllErrors();
            var errorsMap = new HashMap<String, String>();

            for (org.springframework.validation.ObjectError objectError : errorsList) {
                var error = (FieldError) objectError;
                errorsMap.put(error.getField(), error.getDefaultMessage());
            }

            return ResponseEntity.badRequest().body(errorsMap);
        }

        // Проверка email
        if (userRepository.findByEmail(createUserDTO.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "email already exists"));
        }

        try {
            var bCryptEncoder = new BCryptPasswordEncoder();

            User user = new User();
            user.setUsername(createUserDTO.getUsername());
            user.setEmail(createUserDTO.getEmail());
            user.setName(createUserDTO.getName());
            user.setPhoneNumber(createUserDTO.getPhoneNumber());
            user.setRole(Optional.ofNullable(createUserDTO.getRole()).orElse("client"));
            user.setDateOfCreated(new Date());
            user.setPassword(bCryptEncoder.encode(createUserDTO.getPassword()));
            user.setEmailConfirmed(false);

            userRepository.save(user);

            String token = UUID.randomUUID().toString();
            ConfirmationToken confirmationToken = new ConfirmationToken(
                    null,
                    token,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(24),
                    user
            );
            confirmationTokenRepository.save(confirmationToken);

            String confirmUrl = frontendUrl + "/ua/confirm?token=" + token;
            emailService.sendEmail(
                    user.getEmail(),
                    "email_confirmation",
                    Map.of("link", confirmUrl, "username", user.getUsername())
            );

            return ResponseEntity.ok("Confirmation email sent");
        } catch (Exception ex) {
            System.out.println("There is an Exception: ");
            ex.printStackTrace();
        }

        return ResponseEntity.badRequest().body("Error");
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginDTO loginDTO, BindingResult result, HttpServletResponse response) {
        if (result.hasErrors()) {
            var errorsList = result.getAllErrors();
            var errorsMap = new HashMap<String, String>();

            for (org.springframework.validation.ObjectError objectError : errorsList) {
                var error = (FieldError) objectError;
                errorsMap.put(error.getField(), error.getDefaultMessage());
            }

            return ResponseEntity.badRequest().body(errorsMap);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()
                    )
            );

            User user = userRepository.findByEmail(loginDTO.getEmail())
                    .orElseThrow(() -> new RuntimeException("Пользователь с таким email не найден"));

            if (user.getEmailConfirmed() == null || !user.getEmailConfirmed()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Email not confirmed");
            }

            String jwtToken = createJwtToken(user);

            Cookie cookie = new Cookie("Token", jwtToken);
            cookie.setHttpOnly(false); // Делаем cookie недоступной для JavaScript
            cookie.setSecure(false); // Включить, если используешь HTTPS
            cookie.setPath("/"); // Доступно для всех путей приложения
            cookie.setMaxAge(24 * 60 * 60); // Устанавливаем срок действия (1 день)

            response.addCookie(cookie);
            return ResponseEntity.ok("Login successfully done");
        } catch (Exception ex) {
            System.out.println("There is an Exception: ");
            ex.printStackTrace();
        }

        return ResponseEntity.badRequest().body("Bad email or password");
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmEmail(@RequestParam("token") String token) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired");
        }

        User user = confirmationToken.getUser();
        user.setEmailConfirmed(true);
        userRepository.save(user);

        return ResponseEntity.ok("Email confirmed successfully");
    }

    @Transactional
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) {
        confirmationTokenRepository.deleteByUserId(id);
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully!");
    }

    @GetMapping("/allUsers")
    public ResponseEntity<List<UserDTO>> getAllApartments() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable("id") Long id) {
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.ok(userDTO);
    }

    private String createJwtToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(24 * 3600))
                .subject(user.getEmail())
                .claim("role", user.getRole())
                .build();

        var params = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims);
        try {
            var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey.getBytes()));
            return encoder.encode(params).getTokenValue();
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT", e);
        }
    }
}
