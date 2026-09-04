package com.example.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String phoneNumber;
    private String role;
    private String name;
    private String password;
    private String emailConfirmed;
    private String bannedUntil;
}
