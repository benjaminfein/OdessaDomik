package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
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
    @Column(name = "banned_until")
    private Instant bannedUntil;
    @Column(name = "ban_strike_count")
    private Integer banStrikeCount = 0;
    @Column(name = "ban_window_start")
    private Instant banWindowStart;

    public User(Long id, String email, String phoneNumber, String role, String name, String password) {
        this.id = id;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.name = name;
        this.password = password;
    }
}
