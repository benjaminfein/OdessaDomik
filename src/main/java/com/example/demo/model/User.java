package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", unique = true)
    private String username;
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "name")
    private String name;
    @Column(name = "password", length = 1000)
    private String password;
    @Column(name = "role")
    private String role;
    @Column(name = "dateOfCreated")
    private Date dateOfCreated;
    @Column(name = "email_confirmed")
    private Boolean emailConfirmed = false;
    @Column(name = "lang")
    private String lang;

    public User(Long id, String username, String email, String phoneNumber, String role, String name, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.name = name;
        this.password = password;
    }

    public User(String username, String email, String phoneNumber, String role, String name, String password) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.name = name;
        this.password = password;
    }

    public User(@NotEmpty String username,
                @NotEmpty String email,
                String phoneNumber,
                String role,
                @NotEmpty String name,
                @NotEmpty @Size(min = 6, message = "Minimum password length is 6 characters") String password,
                @NotEmpty String lang) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.name = name;
        this.password = password;
        this.lang = lang;
    }
}
