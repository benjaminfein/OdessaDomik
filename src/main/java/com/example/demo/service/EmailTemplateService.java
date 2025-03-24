package com.example.demo.service;

import com.example.demo.model.EmailTemplate;

import java.util.List;

public interface EmailTemplateService {
    String getTemplateBody(String templateKey);

    String getTemplateSubject(String templateKey);

    EmailTemplate getTemplateByKey(String templateKey);

    List<EmailTemplate> getAllTemplates();

    EmailTemplate createTemplate(EmailTemplate template);

    EmailTemplate updateTemplate(Long id, EmailTemplate updatedTemplate);

    void deleteTemplate(Long id);
}
