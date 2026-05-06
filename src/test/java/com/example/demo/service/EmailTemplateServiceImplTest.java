package com.example.demo.service;

import com.example.demo.model.EmailTemplate;
import com.example.demo.repository.EmailTemplateRepository;
import com.example.demo.service.impl.EmailTemplateServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceImplTest {

    @Mock private EmailTemplateRepository emailTemplateRepository;

    @InjectMocks
    private EmailTemplateServiceImpl emailTemplateService;

    private EmailTemplate template(Long id, String key, String lang) {
        return new EmailTemplate(id, key, lang, "Subject " + key, "Body " + key);
    }

    @Test
    void createTemplate_ShouldSaveAndReturn() {
        EmailTemplate t = template(null, "email_confirmation", "ua");
        when(emailTemplateRepository.save(t)).thenReturn(template(1L, "email_confirmation", "ua"));

        EmailTemplate result = emailTemplateService.createTemplate(t);

        assertEquals(1L, result.getId());
        verify(emailTemplateRepository).save(t);
    }

    @Test
    void getTemplateBody_ShouldReturnBody_WhenFound() {
        when(emailTemplateRepository.findByTemplateKeyAndLanguage("email_confirmation", "ua"))
                .thenReturn(Optional.of(template(1L, "email_confirmation", "ua")));

        String body = emailTemplateService.getTemplateBody("email_confirmation", "ua");

        assertEquals("Body email_confirmation", body);
    }

    @Test
    void getTemplateBody_ShouldThrow_WhenNotFound() {
        when(emailTemplateRepository.findByTemplateKeyAndLanguage("missing", "ua"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> emailTemplateService.getTemplateBody("missing", "ua"));
    }

    @Test
    void getTemplateSubject_ShouldReturnSubject_WhenFound() {
        when(emailTemplateRepository.findByTemplateKeyAndLanguage("email_confirmation", "ua"))
                .thenReturn(Optional.of(template(1L, "email_confirmation", "ua")));

        String subject = emailTemplateService.getTemplateSubject("email_confirmation", "ua");

        assertEquals("Subject email_confirmation", subject);
    }

    @Test
    void getTemplateSubject_ShouldThrow_WhenNotFound() {
        when(emailTemplateRepository.findByTemplateKeyAndLanguage("missing", "ua"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> emailTemplateService.getTemplateSubject("missing", "ua"));
    }

    @Test
    void getTemplateByKeyAndLanguage_ShouldReturn_WhenFound() {
        EmailTemplate t = template(1L, "email_confirmation", "ua");
        when(emailTemplateRepository.findByTemplateKeyAndLanguage("email_confirmation", "ua"))
                .thenReturn(Optional.of(t));

        EmailTemplate result = emailTemplateService.getTemplateByKeyAndLanguage("email_confirmation", "ua");

        assertEquals(1L, result.getId());
    }

    @Test
    void getTemplateByKeyAndLanguage_ShouldThrow_WhenNotFound() {
        when(emailTemplateRepository.findByTemplateKeyAndLanguage("missing", "ua"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> emailTemplateService.getTemplateByKeyAndLanguage("missing", "ua"));
    }

    @Test
    void getAllTemplates_ShouldReturnAll() {
        when(emailTemplateRepository.findAll())
                .thenReturn(List.of(template(1L, "k1", "ua"), template(2L, "k2", "ru")));

        List<EmailTemplate> result = emailTemplateService.getAllTemplates();

        assertEquals(2, result.size());
    }

    @Test
    void updateTemplate_ShouldUpdateFields_WhenFound() {
        EmailTemplate existing = template(1L, "email_confirmation", "ua");
        EmailTemplate update = new EmailTemplate(null, "email_confirmation", "ua", "New Subject", "New Body");
        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(emailTemplateRepository.save(any())).thenReturn(existing);

        EmailTemplate result = emailTemplateService.updateTemplate(1L, update);

        assertEquals("New Subject", result.getSubject());
        assertEquals("New Body", result.getBody());
        verify(emailTemplateRepository).save(existing);
    }

    @Test
    void updateTemplate_ShouldThrow_WhenNotFound() {
        when(emailTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> emailTemplateService.updateTemplate(99L, template(null, "k", "ua")));
    }

    @Test
    void deleteTemplate_ShouldCallRepository() {
        emailTemplateService.deleteTemplate(1L);

        verify(emailTemplateRepository).deleteById(1L);
    }
}
