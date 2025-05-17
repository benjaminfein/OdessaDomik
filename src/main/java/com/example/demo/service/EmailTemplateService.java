package com.example.demo.service;

import com.example.demo.model.EmailTemplate;

import java.util.List;

public interface EmailTemplateService {
    String getTemplateBody(String templateKey, String language);

    String getTemplateSubject(String templateKey, String language);

    EmailTemplate getTemplateByKeyAndLanguage(String templateKey, String language);

    List<EmailTemplate> getAllTemplates();

    EmailTemplate createTemplate(EmailTemplate template);

    EmailTemplate updateTemplate(Long id, EmailTemplate updatedTemplate);

    void deleteTemplate(Long id);
}
