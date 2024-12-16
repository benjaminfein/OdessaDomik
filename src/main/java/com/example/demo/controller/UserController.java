//package com.example.demo.controller;
//
//
//import com.example.demo.dto.user.UserDTO;
//import com.example.demo.service.UserService;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@CrossOrigin("*")
//@Slf4j
//@RestController
//@RequestMapping("/api/user")
//@AllArgsConstructor
//public class UserController {
//    private UserService userService;
//
//    @PostMapping
//    public ResponseEntity<CreateUserDTO> createUser(@RequestBody UserDTO userDTO) {
//        CreateUserDTO createUserDTO = userService.createUser(userDTO);
//        return new ResponseEntity<>(createUserDTO, HttpStatus.CREATED);
//    }
//}
