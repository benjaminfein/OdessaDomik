package com.example.demo.controller;

import com.example.demo.dto.ApartmentDTO;
import com.example.demo.dto.BookedDateRangeDTO;
import com.example.demo.dto.ReservationDTO;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.exception.ApartmentNotFoundException;
import com.example.demo.service.ApartmentService;
import com.example.demo.service.DateRangeRuleService;
import com.example.demo.service.ReservationService;
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
class ApartmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ApartmentService apartmentService;
    @MockBean private ReservationService reservationService;
    @MockBean private DateRangeRuleService dateRangeRuleService;
    @MockBean private JavaMailSenderImpl javaMailSender;

    private ApartmentDTO sampleDto() {
        ApartmentDTO dto = new ApartmentDTO();
        dto.setId(1L);
        dto.setName("Test Apartment");
        dto.setPrice(1000);
        dto.setCountOfSleepPlaces(2);
        return dto;
    }

    @Test
    void getAllApartments_ShouldReturn200_WithoutAuth() throws Exception {
        when(apartmentService.getAllApartments()).thenReturn(List.of(sampleDto()));

        mockMvc.perform(get("/api/apartments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Apartment"));
    }

    @Test
    void getApartmentById_ShouldReturn200_WhenFound() throws Exception {
        when(apartmentService.getApartmentById(1L)).thenReturn(sampleDto());

        mockMvc.perform(get("/api/apartments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Apartment"));
    }

    @Test
    void getApartmentById_ShouldReturn404_WhenNotFound() throws Exception {
        when(apartmentService.getApartmentById(99L)).thenThrow(new ApartmentNotFoundException("Not found"));

        mockMvc.perform(get("/api/apartments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createApartment_ShouldReturn201_WhenAdmin() throws Exception {
        ApartmentDTO dto = sampleDto();
        dto.setId(null);
        when(apartmentService.createApartment(any())).thenReturn(sampleDto());

        mockMvc.perform(post("/api/apartments")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Apartment"));
    }

    @Test
    void createApartment_ShouldReturn201_WhenNotAuthenticated_OnDev() throws Exception {
        when(apartmentService.createApartment(any())).thenReturn(sampleDto());

        mockMvc.perform(post("/api/apartments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto())))
                .andExpect(status().isCreated());
    }

    @Test
    void updateApartment_ShouldReturn200_WhenAdmin() throws Exception {
        when(apartmentService.updateApartment(eq(1L), any())).thenReturn(sampleDto());

        mockMvc.perform(put("/api/apartments/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Apartment"));
    }

    @Test
    void deleteApartment_ShouldReturn200_WhenAdmin() throws Exception {
        doNothing().when(apartmentService).deleteApartment(1L);

        mockMvc.perform(delete("/api/apartments/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Apartment deleted successfully!"));
    }

    @Test
    void getAvailableApartments_ShouldReturn200_WithDates() throws Exception {
        when(apartmentService.findAvailableApartments(any(), any(), eq(2))).thenReturn(List.of(sampleDto()));

        mockMvc.perform(get("/api/apartments/available")
                        .param("checkIn", "2025-07-01")
                        .param("checkOut", "2025-07-07")
                        .param("guestCount", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Apartment"));
    }

    @Test
    void getAvailableApartments_ShouldReturn200_WithoutDates() throws Exception {
        when(apartmentService.findApartmentsByGuestCount(2)).thenReturn(List.of(sampleDto()));

        mockMvc.perform(get("/api/apartments/available").param("guestCount", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllReservations_ShouldReturn200_WhenAdmin() throws Exception {
        ReservationDTO res = new ReservationDTO(1L, 1L,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7),
                2L, ReservationStatus.CONFIRMED, 1L, "john@test.com", "ua", null, null);
        when(reservationService.getAllReservations()).thenReturn(List.of(res));

        mockMvc.perform(get("/api/apartments/get-reservations")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientEmail").value("john@test.com"));
    }

    @Test
    void getAllReservations_ShouldReturn200_WhenClientRole_OnDev() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(List.of());

        mockMvc.perform(get("/api/apartments/get-reservations")
                        .with(jwt().authorities(new SimpleGrantedAuthority("client"))))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReservation_ShouldReturn200_WhenAdmin() throws Exception {
        doNothing().when(reservationService).deleteReservation(1L);

        mockMvc.perform(delete("/api/apartments/delete-reservation/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk());
    }

    @Test
    void deletePendingReservations_ShouldReturn200_WhenAdmin() throws Exception {
        doNothing().when(reservationService).deletePendingReservations();

        mockMvc.perform(delete("/api/apartments/delete-pending")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk())
                .andExpect(content().string("All pending reservations deleted."));
    }

    @Test
    void confirmReservation_ShouldReturn200_WhenAdmin() throws Exception {
        doNothing().when(reservationService).confirmReservation(1L);

        mockMvc.perform(put("/api/apartments/confirm-reservation/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk());
    }

    @Test
    void cancelReservation_ShouldReturn200_WhenAdmin() throws Exception {
        doNothing().when(reservationService).cancelReservation(1L);

        mockMvc.perform(put("/api/apartments/cancel-reservation/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isOk());
    }

    @Test
    void placeReservationOnHold_ShouldReturn200_WhenAuthenticated() throws Exception {
        doNothing().when(reservationService).pendingReservation(1L);

        mockMvc.perform(put("/api/apartments/reservation-on-hold/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("client"))))
                .andExpect(status().isOk());
    }

    @Test
    void getBookedDates_ShouldReturn200() throws Exception {
        BookedDateRangeDTO range = new BookedDateRangeDTO(
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7), LocalDate.of(2025, 7, 8));
        when(reservationService.getBookedDateRanges(1L)).thenReturn(List.of(range));

        mockMvc.perform(get("/api/apartments/1/booked-dates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].checkIn").value("2025-07-01"));
    }

    @Test
    void getMinStay_ShouldReturn200() throws Exception {
        when(dateRangeRuleService.getEffectiveMinStay(eq(1L), any())).thenReturn(3);

        mockMvc.perform(get("/api/apartments/1/min-stay").param("checkIn", "2025-07-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minStay").value(3));
    }

    @Test
    void getPrice_ShouldReturn200() throws Exception {
        when(reservationService.calculatePrice(eq(1L), any(), any(), eq(2L))).thenReturn(5000);

        mockMvc.perform(get("/api/apartments/1/price")
                        .param("checkIn", "2025-07-01")
                        .param("checkOut", "2025-07-07")
                        .param("guestCount", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(5000));
    }

    @Test
    void createReservation_ShouldReturn201_WhenAuthenticated() throws Exception {
        ReservationDTO dto = new ReservationDTO(null, 1L,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7),
                2L, ReservationStatus.PENDING, 1L, "john@test.com", "ua", null, null);
        ReservationDTO saved = new ReservationDTO(1L, 1L,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7),
                2L, ReservationStatus.PENDING, 1L, "john@test.com", "ua", 5000, null);
        when(reservationService.createReservation(any())).thenReturn(saved);

        mockMvc.perform(post("/api/apartments/create-reservation")
                        .with(jwt().authorities(new SimpleGrantedAuthority("client")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getReservationsByApartment_ShouldReturn200_WhenAdmin() throws Exception {
        ReservationDTO res = new ReservationDTO(1L, 1L,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7),
                2L, ReservationStatus.CONFIRMED, 1L, "john@test.com", "ua", null, null);
        when(reservationService.getReservationsByApartmentAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of(res));

        mockMvc.perform(get("/api/apartments/1/reservations")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin")))
                        .param("from", "2025-07-01")
                        .param("to", "2025-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientEmail").value("john@test.com"));
    }
}
