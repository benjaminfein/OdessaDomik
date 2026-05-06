package com.example.demo.controller;

import com.example.demo.model.EmailTemplate;
import com.example.demo.service.EmailTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
class EmailTemplateControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private EmailTemplateService emailTemplateService;
    @MockBean private JavaMailSenderImpl javaMailSender;

    private EmailTemplate template(Long id) {
        return new EmailTemplate(id, "email_confirmation", "ua", "Confirm email", "Click {{link}}");
    }

    // --- POST /api/email-templates/create-template (admin only) ---

    @Test
    void createTemplate_ShouldReturn200_WhenAdmin() throws Exception {
        EmailTemplate input = template(null);
        when(emailTemplateService.createTemplate(any())).thenReturn(template(1L));

        mockMvc.perform(post("/api/email-templates/create-template")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.templateKey").value("email_confirmation"));
    }

    // On dev profile admin endpoints are open to all
    @Test
    void createTemplate_ShouldReturn200_WhenNoAuth_OnDev() throws Exception {
        when(emailTemplateService.createTemplate(any())).thenReturn(template(1L));

        mockMvc.perform(post("/api/email-templates/create-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template(null))))
                .andExpect(status().isOk());
    }

    // --- GET /api/email-templates/get-all-templates (admin only) ---

    @Test
    void getAllTemplates_ShouldReturn200_WhenAdmin() throws Exception {
        when(emailTemplateService.getAllTemplates())
                .thenReturn(List.of(template(1L), template(2L)));

        mockMvc.perform(get("/api/email-templates/get-all-templates")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // On dev profile admin endpoints are open — no auth still returns 200
    @Test
    void getAllTemplates_ShouldReturn200_WhenNoAuth_OnDev() throws Exception {
        when(emailTemplateService.getAllTemplates()).thenReturn(List.of());

        mockMvc.perform(get("/api/email-templates/get-all-templates"))
                .andExpect(status().isOk());
    }

    // --- GET /api/email-templates/get-template-by-key (admin only) ---

    @Test
    void getTemplateByKey_ShouldReturn200_WhenAdmin() throws Exception {
        when(emailTemplateService.getTemplateByKeyAndLanguage("email_confirmation", "ua"))
                .thenReturn(template(1L));

        mockMvc.perform(get("/api/email-templates/get-template-by-key")
                        .param("templateKey", "email_confirmation")
                        .param("language", "ua")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateKey").value("email_confirmation"));
    }

    // --- PUT /api/email-templates/update-template/{id} (admin only) ---

    @Test
    void updateTemplate_ShouldReturn200_WhenAdmin() throws Exception {
        EmailTemplate updated = new EmailTemplate(1L, "email_confirmation", "ua",
                "New Subject", "New Body");
        when(emailTemplateService.updateTemplate(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/email-templates/update-template/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("New Subject"));
    }

    // --- DELETE /api/email-templates/delete-template/{id} (admin only) ---

    @Test
    void deleteTemplate_ShouldReturn200_WhenAdmin() throws Exception {
        doNothing().when(emailTemplateService).deleteTemplate(1L);

        mockMvc.perform(delete("/api/email-templates/delete-template/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Template deleted successfully!"));
    }

    // On dev profile admin endpoints are open — no auth still returns 200
    @Test
    void deleteTemplate_ShouldReturn200_WhenNoAuth_OnDev() throws Exception {
        doNothing().when(emailTemplateService).deleteTemplate(1L);

        mockMvc.perform(delete("/api/email-templates/delete-template/1"))
                .andExpect(status().isOk());
    }
}
