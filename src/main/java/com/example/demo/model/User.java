//package com.example.demo.model;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.Date;
//
//@Entity
//@Table(name = "users")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class User {
////    implements UserDetails {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    @Column(name = "username", unique = true)
//    private String username;
//    @Column(name = "email", unique = true)
//    private String email;
//    @Column(name = "phone_number")
//    private String phoneNumber;
//    @Column(name = "name")
//    private String name;
//    @Column(name = "active")
//    private boolean active;
//    @Column(name = "password", length = 1000)
//    private String password;
////    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
////    @CollectionTable(name = "user_role",
////    joinColumns = @JoinColumn(name = "user_id"))
////    @Enumerated(EnumType.STRING)
////    Set<Role>
//    private String roles;
//    private Date dateOfCreated;
//
//    public User(Long id, String username, String email, String phoneNumber, String name, boolean active, String password) {
//    }
//
//    public User(String email, String phoneNumber, String name, String password) {
//    }
////
////    public User(Long id, String email, String phoneNumber, String name, boolean active, String password) {
////    }
////
////    public User(String email, String phoneNumber, String name, String password) {
////    }
////
////    @PrePersist
////    private void init() {
////        dateOfCreated = LocalDateTime.now();
////    }
////
////    //security
////
////    @Override
////    public Collection<? extends GrantedAuthority> getAuthorities() {
////        return roles;
////    }
////
////    @Override
////    public String getUsername() {
////        return email;
////    }
////
////    @Override
////    public boolean isAccountNonExpired() {
////        return true;
////    }
////
////    @Override
////    public boolean isAccountNonLocked() {
////        return true;
////    }
////
////    @Override
////    public boolean isCredentialsNonExpired() {
////        return true;
////    }
////
////    @Override
////    public boolean isEnabled() {
////        return active;
////    }
//}
