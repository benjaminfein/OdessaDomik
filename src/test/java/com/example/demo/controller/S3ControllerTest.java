package com.example.demo.controller;

import com.example.demo.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mock.web.MockMultipartFile;
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
class S3ControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private S3Service s3Service;
    @MockBean private JavaMailSenderImpl javaMailSender;

    // --- GET /api/s3/list/{apartmentId} (public) ---

    @Test
    void listFiles_ShouldReturn200_WhenPublic() throws Exception {
        when(s3Service.listFiles(1L)).thenReturn(List.of(
                "https://bucket.s3.eu-central-1.amazonaws.com/apartments/1/photo.jpg"));

        mockMvc.perform(get("/api/s3/list/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(
                        "https://bucket.s3.eu-central-1.amazonaws.com/apartments/1/photo.jpg"));
    }

    @Test
    void listFiles_ShouldReturnEmptyList_WhenNoFiles() throws Exception {
        when(s3Service.listFiles(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/s3/list/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- POST /api/s3/upload-multiple (admin only) ---

    @Test
    void uploadFiles_ShouldReturn200_WhenAdmin() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "photo.jpg", "image/jpeg", "content".getBytes());
        doNothing().when(s3Service).uploadFiles(anyList(), eq(1L));

        mockMvc.perform(multipart("/api/s3/upload-multiple")
                        .file(file)
                        .param("apartmentId", "1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Files uploaded and attached to apartment with id \"1\" successfully."));
    }

    // On dev profile admin endpoints are open — upload works without auth
    @Test
    void uploadFiles_ShouldReturn200_WhenNoAuth_OnDev() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "photo.jpg", "image/jpeg", "content".getBytes());
        doNothing().when(s3Service).uploadFiles(anyList(), eq(1L));

        mockMvc.perform(multipart("/api/s3/upload-multiple")
                        .file(file)
                        .param("apartmentId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void uploadFiles_ShouldReturn500_WhenServiceThrowsIOException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "photo.jpg", "image/jpeg", "content".getBytes());
        doThrow(new java.io.IOException("S3 error")).when(s3Service).uploadFiles(anyList(), eq(1L));

        mockMvc.perform(multipart("/api/s3/upload-multiple")
                        .file(file)
                        .param("apartmentId", "1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("S3 error")));
    }

    // --- DELETE /api/s3/delete-multiple/{apartmentId} (admin only) ---

    @Test
    void deleteFiles_ShouldReturn200_WhenAdmin() throws Exception {
        doNothing().when(s3Service).deleteFiles(eq(1L), anyList());

        mockMvc.perform(delete("/api/s3/delete-multiple/1")
                        .param("fileNames", "photo.jpg")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Files deleted successfully."));
    }

    // On dev profile admin endpoints are open — delete works without auth
    @Test
    void deleteFiles_ShouldReturn200_WhenNoAuth_OnDev() throws Exception {
        doNothing().when(s3Service).deleteFiles(eq(1L), anyList());

        mockMvc.perform(delete("/api/s3/delete-multiple/1")
                        .param("fileNames", "photo.jpg"))
                .andExpect(status().isOk());
    }
}
