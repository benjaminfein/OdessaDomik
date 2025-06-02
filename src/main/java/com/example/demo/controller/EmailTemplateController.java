package com.example.demo.controller;

import com.example.demo.model.EmailTemplate;
import com.example.demo.service.EmailTemplateService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email-templates")
@AllArgsConstructor
public class EmailTemplateController {
    private final EmailTemplateService emailTemplateService;

    @PostMapping("/create-template")
    public ResponseEntity<EmailTemplate> createTemplate(@RequestBody EmailTemplate template) {
        return ResponseEntity.ok(emailTemplateService.createTemplate(template));
    }

    @GetMapping("/get-template-by-key")
    public ResponseEntity<EmailTemplate> getTemplateByKey(
            @RequestParam String templateKey,
            @RequestParam String language
    ) {
        return ResponseEntity.ok(
                emailTemplateService.getTemplateByKeyAndLanguage(templateKey, language)
        );
    }

    @GetMapping("/get-all-templates")
    public ResponseEntity<List<EmailTemplate>> getAllTemplates() {
        return ResponseEntity.ok(emailTemplateService.getAllTemplates());
    }

    @PutMapping("/update-template/{id}")
    public ResponseEntity<EmailTemplate> updateTemplate(@PathVariable Long id, @RequestBody EmailTemplate template) {
        return ResponseEntity.ok(emailTemplateService.updateTemplate(id, template));
    }

    @DeleteMapping("/delete-template/{id}")
    public ResponseEntity<String> deleteTemplate(@PathVariable Long id) {
        emailTemplateService.deleteTemplate(id);
        return ResponseEntity.ok("Reservation deleted successfully!");
    }
}