package com.example.demo.dto.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
    @NotEmpty
    private String email;
    @NotEmpty
    private String name;
    private String phoneNumber;
    private String role;

    @NotEmpty
    @Size(min = 6, message = "Minimum password length is 6 characters")
    private String password;

    @NotEmpty
    private String lang;
}
