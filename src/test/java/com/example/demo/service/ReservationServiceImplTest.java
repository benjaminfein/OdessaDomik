package com.example.demo.service;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.exception.ReservationNotFoundException;
import com.example.demo.model.Apartment;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.ReservationServiceImpl;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private ApartmentRepository apartmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User user;
    private User admin;
    private Apartment apartment;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        user = new User(1L, "john", "john@test.com", "+380991234567", "client", "John Doe", "encoded");
        admin = new User(2L, "admin", "admin@test.com", "+380997654321", "admin", "Admin User", "encoded");

        apartment = new Apartment(1L, "Apt 1", "Short", "Desc",
                "Гагаринское плато 5/2", 1000, true, true, 3, 60, true, 4, new ArrayList<>());

        reservation = new Reservation(1L, apartment,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7),
                2L, ReservationStatus.PENDING, user, "john@test.com", "ua");
    }

    @Test
    void findBookedApartmentIds_ShouldDelegateToRepository() {
        LocalDate start = LocalDate.of(2025, 7, 1);
        LocalDate end = LocalDate.of(2025, 7, 7);
        when(reservationRepository.findBookedApartmentIds(start, end)).thenReturn(List.of(1L, 2L));

        List<Long> result = reservationService.findBookedApartmentIds(start, end);

        assertEquals(List.of(1L, 2L), result);
        verify(reservationRepository).findBookedApartmentIds(start, end);
    }

    @Test
    void getAllReservations_ShouldReturnMappedDTOs() {
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));

        List<ReservationDTO> result = reservationService.getAllReservations();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("john@test.com", result.get(0).getClientEmail());
    }

    @Test
    void deleteReservation_ShouldDelete_WhenFound() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.deleteReservation(1L);

        verify(reservationRepository).deleteById(1L);
    }

    @Test
    void deleteReservation_ShouldThrow_WhenNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class, () -> reservationService.deleteReservation(99L));
        verify(reservationRepository, never()).deleteById(any());
    }

    @Test
    void deletePendingReservations_ShouldCallRepositoryDelete() {
        reservationService.deletePendingReservations();

        verify(reservationRepository).deleteByStatus(ReservationStatus.PENDING);
    }

    @Test
    void pendingReservation_ShouldSetStatusAndSendEmails() throws MessagingException {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(userRepository.findByRole("admin")).thenReturn(List.of(admin));

        reservationService.pendingReservation(1L);

        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        verify(reservationRepository).save(reservation);
        verify(emailService, atLeastOnce()).sendEmail(eq("john@test.com"), eq("reservation_pending"), anyMap());
        verify(emailService, atLeastOnce()).sendEmail(eq("admin@test.com"), eq("reservation_pending_admin"), anyMap());
    }

    @Test
    void pendingReservation_ShouldThrow_WhenNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reservationService.pendingReservation(99L));
    }

    @Test
    void cancelReservation_ShouldSetStatusAndSendEmails() throws MessagingException {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(userRepository.findByRole("admin")).thenReturn(List.of(admin));

        reservationService.cancelReservation(1L);

        assertEquals(ReservationStatus.CANCELED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
        verify(emailService, atLeastOnce()).sendEmail(eq("john@test.com"), eq("reservation_canceled"), anyMap());
        verify(emailService, atLeastOnce()).sendEmail(eq("admin@test.com"), eq("reservation_canceled_admin"), anyMap());
    }

    @Test
    void cancelReservation_ShouldThrow_WhenNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reservationService.cancelReservation(99L));
    }

    @Test
    void confirmReservation_ShouldSetStatusAndSendEmails() throws MessagingException {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(userRepository.findByRole("admin")).thenReturn(List.of(admin));

        reservationService.confirmReservation(1L);

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
        verify(emailService, atLeastOnce()).sendEmail(eq("john@test.com"), eq("reservation_confirmed"), anyMap());
        verify(emailService, atLeastOnce()).sendEmail(eq("admin@test.com"), eq("reservation_confirmed_admin"), anyMap());
    }

    @Test
    void confirmReservation_ShouldThrow_WhenNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reservationService.confirmReservation(99L));
    }
}
