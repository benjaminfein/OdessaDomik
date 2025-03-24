package com.example.demo.service;

import jakarta.mail.MessagingException;

import java.util.Map;

public interface EmailService {
    void sendEmail(String to, String templateKey, Map<String, String> placeholders) throws MessagingException;
}
