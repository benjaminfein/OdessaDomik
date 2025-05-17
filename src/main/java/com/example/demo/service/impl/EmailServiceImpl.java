package com.example.demo.service.impl;

import com.example.demo.service.EmailService;
import com.example.demo.service.EmailTemplateService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendEmail(String to, String templateKey, Map<String, String> placeholders) throws MessagingException {
        String lang = placeholders.getOrDefault("lang", "ua");

        String subject = emailTemplateService.getTemplateSubject(templateKey, lang);
        String body = emailTemplateService.getTemplateBody(templateKey, lang);

        if (body != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                body = body.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }
        if (subject != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                subject = subject.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("your-email@example.com");
            helper.setText(body, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Ошибка при отправке письма", e);
            throw e; // не забувай пробросити виняток наверх
        }
    }
}
