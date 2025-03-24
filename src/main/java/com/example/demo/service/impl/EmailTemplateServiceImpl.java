package com.example.demo.service.impl;

import com.example.demo.model.EmailTemplate;
import com.example.demo.repository.EmailTemplateRepository;
import com.example.demo.service.EmailTemplateService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {
    private final EmailTemplateRepository emailTemplateRepository;

    @Override
    public EmailTemplate createTemplate(EmailTemplate template) {
        return emailTemplateRepository.save(template);
    }

    @Override
    public String getTemplateBody(String templateKey) {
        Optional<EmailTemplate> template = emailTemplateRepository.findByTemplateKey(templateKey);
        return template.map(EmailTemplate::getBody)
                .orElseThrow(() -> new RuntimeException("Шаблон " + templateKey + " не найден"));
    }

    @Override
    public String getTemplateSubject(String templateKey) {
        Optional<EmailTemplate> template = emailTemplateRepository.findByTemplateKey(templateKey);
        return template.map(EmailTemplate::getSubject)
                .orElseThrow(() -> new RuntimeException("Шаблон " + templateKey + " не найден"));
    }

    @Override
    public EmailTemplate getTemplateByKey(String templateKey) {
        return emailTemplateRepository.findByTemplateKey(templateKey)
                .orElseThrow(() -> new RuntimeException("Шаблон не найден"));
    }

    @Override
    public List<EmailTemplate> getAllTemplates() {
        return emailTemplateRepository.findAll();
    }

    @Override
    public EmailTemplate updateTemplate(Long id, EmailTemplate updatedTemplate) {
        EmailTemplate existingTemplate = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Шаблон не найден"));

        existingTemplate.setTemplateKey(updatedTemplate.getTemplateKey());
        existingTemplate.setSubject(updatedTemplate.getSubject());
        existingTemplate.setBody(updatedTemplate.getBody());

        return emailTemplateRepository.save(existingTemplate);
    }

    @Override
    public void deleteTemplate(Long id) {
        emailTemplateRepository.deleteById(id);
    }
}