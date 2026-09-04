package com.example.demo.controller;

import com.example.demo.dto.DateRangeRuleDTO;
import com.example.demo.exception.ApartmentNotFoundException;
import com.example.demo.service.DateRangeRuleService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
class DateRangeRuleControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DateRangeRuleService dateRangeRuleService;
    @MockBean private JavaMailSenderImpl javaMailSender;

    private DateRangeRuleDTO sampleDto() {
        DateRangeRuleDTO dto = new DateRangeRuleDTO();
        dto.setId(1L);
        dto.setApartmentId(1L);
        dto.setStartDate(LocalDate.of(2025, 7, 1));
        dto.setEndDate(LocalDate.of(2025, 7, 7));
        dto.setStatus("OPEN");
        dto.setPriceOverride(1500);
        dto.setPriceUnit("UAH");
        dto.setMinStay(2);
        return dto;
    }

    @Test
    void getRules_ShouldReturn200_WhenAdmin() throws Exception {
        when(dateRangeRuleService.getRules(eq(1L), any(), any())).thenReturn(List.of(sampleDto()));

        mockMvc.perform(get("/api/apartments/1/rules")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin")))
                        .param("from", "2025-07-01")
                        .param("to", "2025-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    void saveRule_ShouldReturn201_WhenAdmin() throws Exception {
        DateRangeRuleDTO requestDto = sampleDto();
        requestDto.setId(null);
        when(dateRangeRuleService.saveRule(eq(1L), any())).thenReturn(sampleDto());

        mockMvc.perform(post("/api/apartments/1/rules")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.priceOverride").value(1500));
    }

    @Test
    void saveRule_ShouldReturn404_WhenApartmentNotFound() throws Exception {
        when(dateRangeRuleService.saveRule(eq(99L), any()))
                .thenThrow(new ApartmentNotFoundException("Apartment not found: 99"));

        mockMvc.perform(post("/api/apartments/99/rules")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRule_ShouldReturn204_WhenAdmin() throws Exception {
        doNothing().when(dateRangeRuleService).deleteRule(1L);

        mockMvc.perform(delete("/api/apartments/1/rules/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isNoContent());

        verify(dateRangeRuleService).deleteRule(1L);
    }
}
