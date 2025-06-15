package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MailVerifier {

    private final JavaMailSenderImpl mailSender;

    @Autowired
    public MailVerifier(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void testMailConnection() {
        try {
            mailSender.testConnection();
            log.info("[MailVerifier] SMTP login successful");
        } catch (Exception e) {
            log.error("[MailVerifier] SMTP login failed", e);
        }
    }
}