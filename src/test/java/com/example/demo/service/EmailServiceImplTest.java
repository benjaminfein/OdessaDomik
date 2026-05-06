package com.example.demo.service;

import com.example.demo.service.impl.EmailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock private JavaMailSender mailSender;
    @Mock private EmailTemplateService emailTemplateService;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendEmail_ShouldSubstitutePlaceholdersAndSend() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailTemplateService.getTemplateSubject("email_confirmation", "ua"))
                .thenReturn("Hello {{username}}");
        when(emailTemplateService.getTemplateBody("email_confirmation", "ua"))
                .thenReturn("<p>Click {{link}}</p>");

        emailService.sendEmail("to@test.com", "email_confirmation",
                Map.of("username", "Alice", "link", "http://example.com", "lang", "ua"));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_ShouldThrow_WhenMailSenderFails() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailTemplateService.getTemplateSubject("email_confirmation", "ua")).thenReturn("Subject");
        when(emailTemplateService.getTemplateBody("email_confirmation", "ua")).thenReturn("Body");
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () ->
                emailService.sendEmail("to@test.com", "email_confirmation",
                        Map.of("lang", "ua")));
    }
}
