package com.example.demo.service;

import com.example.demo.dto.BookedDateRangeDTO;
import com.example.demo.dto.ReservationDTO;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.enums.RuleStatus;
import com.example.demo.exception.MinStayViolationException;
import com.example.demo.exception.ReservationConflictException;
import com.example.demo.exception.ReservationNotFoundException;
import com.example.demo.model.Apartment;
import com.example.demo.model.DateRangeRule;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.repository.DateRangeRuleRepository;
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
    @Mock private DateRangeRuleRepository dateRangeRuleRepository;
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
                "Гагаринское плато 5/2", 1000, true, true, 3, 60, true, 4, 0,
                false, null, null, false, null, null, new ArrayList<>());

        reservation = new Reservation(1L, apartment,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7),
                2L, ReservationStatus.PENDING, user, "john@test.com", "ua", null);
    }

    // --- findBookedApartmentIds ---

    @Test
    void findBookedApartmentIds_ShouldDelegateToRepository() {
        LocalDate start = LocalDate.of(2025, 7, 1);
        LocalDate end = LocalDate.of(2025, 7, 7);
        when(reservationRepository.findBookedApartmentIds(start, end)).thenReturn(List.of(1L, 2L));

        List<Long> result = reservationService.findBookedApartmentIds(start, end);

        assertEquals(List.of(1L, 2L), result);
        verify(reservationRepository).findBookedApartmentIds(start, end);
    }

    // --- getBookedDateRanges ---

    @Test
    void getBookedDateRanges_ShouldReturnRanges_WithGapDaysZero() {
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(reservationRepository.findByApartment_IdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of(reservation));

        List<BookedDateRangeDTO> result = reservationService.getBookedDateRanges(1L);

        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2025, 7, 1), result.get(0).getCheckIn());
        assertEquals(LocalDate.of(2025, 7, 7), result.get(0).getCheckOut());
        assertEquals(LocalDate.of(2025, 7, 7), result.get(0).getEffectiveCheckOut());
    }

    @Test
    void getBookedDateRanges_ShouldApplyGapDays_ToEffectiveCheckOut() {
        Apartment aptWithGap = new Apartment(1L, "Apt 1", "Short", "Desc",
                "Гагаринское плато 5/2", 1000, true, true, 3, 60, true, 4, 2,
                false, null, null, false, null, null, new ArrayList<>());
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(aptWithGap));
        when(reservationRepository.findByApartment_IdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of(reservation));

        List<BookedDateRangeDTO> result = reservationService.getBookedDateRanges(1L);

        // checkOut=Jul 7 + gapDays=2 → effectiveCheckOut=Jul 9
        assertEquals(LocalDate.of(2025, 7, 9), result.get(0).getEffectiveCheckOut());
    }

    @Test
    void getBookedDateRanges_ShouldReturnEmpty_WhenNoActiveReservations() {
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(reservationRepository.findByApartment_IdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of());

        List<BookedDateRangeDTO> result = reservationService.getBookedDateRanges(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getBookedDateRanges_ShouldThrow_WhenApartmentNotFound() {
        when(apartmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reservationService.getBookedDateRanges(99L));
    }

    // --- createReservation ---

    @Test
    void createReservation_ShouldSave_WhenNoExistingReservations() {
        ReservationDTO dto = new ReservationDTO(null, 1L,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 5),
                2L, ReservationStatus.PENDING, 1L, "john@test.com", "ua", null, null);

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(reservationRepository.findByApartment_IdAndStatusIn(eq(1L), anyList())).thenReturn(List.of());
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(reservationRepository.save(any())).thenReturn(reservation);

        ReservationDTO result = reservationService.createReservation(dto);

        assertNotNull(result);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_ShouldThrowConflict_WhenDatesOverlapWithConfirmed() {
        Reservation confirmed = new Reservation(2L, apartment,
                LocalDate.of(2025, 7, 10), LocalDate.of(2025, 7, 20),
                2L, ReservationStatus.CONFIRMED, user, "john@test.com", "ua", null);

        // New reservation partially overlaps: 15 Jul – 25 Jul
        ReservationDTO dto = new ReservationDTO(null, 1L,
                LocalDate.of(2025, 7, 15), LocalDate.of(2025, 7, 25),
                2L, ReservationStatus.PENDING, 1L, "john@test.com", "ua", null, null);

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(reservationRepository.findByApartment_IdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of(confirmed));

        assertThrows(ReservationConflictException.class, () -> reservationService.createReservation(dto));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_ShouldThrowConflict_WhenDatesOverlapWithPending() {
        Reservation pending = new Reservation(2L, apartment,
                LocalDate.of(2025, 7, 10), LocalDate.of(2025, 7, 20),
                2L, ReservationStatus.PENDING, user, "john@test.com", "ua", null);

        ReservationDTO dto = new ReservationDTO(null, 1L,
                LocalDate.of(2025, 7, 5), LocalDate.of(2025, 7, 15),
                2L, ReservationStatus.PENDING, 1L, "john@test.com", "ua", null, null);

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(reservationRepository.findByApartment_IdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of(pending));

        assertThrows(ReservationConflictException.class, () -> reservationService.createReservation(dto));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_ShouldAllow_WhenNewCheckInEqualsExistingCheckOut_GapDaysZero() {
        // Existing: Jul 1 – Jul 7. New: Jul 7 – Jul 12. gapDays=0 → allowed (day of checkout is free)
        Reservation existing = new Reservation(2L, apartment,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7),
                2L, ReservationStatus.CONFIRMED, user, "john@test.com", "ua", null);

        ReservationDTO dto = new ReservationDTO(null, 1L,
                LocalDate.of(2025, 7, 7), LocalDate.of(2025, 7, 12),
                2L, ReservationStatus.PENDING, 1L, "john@test.com", "ua", null, null);

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(reservationRepository.findByApartment_IdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of(existing));
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(reservationRepository.save(any())).thenReturn(reservation);

        assertDoesNotThrow(() -> reservationService.createReservation(dto));
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_ShouldThrowConflict_WhenNewCheckInEqualsExistingCheckOut_GapDaysOne() {
        // Existing: Jul 1 – Jul 7. gapDays=1 → effectiveEnd=Jul 8. New checkIn=Jul 7 < Jul 8 → conflict
        Apartment aptWithGap = new Apartment(1L, "Apt 1", "Short", "Desc",
                "Гагаринское плато 5/2", 1000, true, true, 3, 60, true, 4, 1,
                false, null, null, false, null, null, new ArrayList<>());
        Reservation existing = new Reservation(2L, aptWithGap,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7),
                2L, ReservationStatus.CONFIRMED, user, "john@test.com", "ua", null);

        ReservationDTO dto = new ReservationDTO(null, 1L,
                LocalDate.of(2025, 7, 7), LocalDate.of(2025, 7, 12),
                2L, ReservationStatus.PENDING, 1L, "john@test.com", "ua", null, null);

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(aptWithGap));
        when(reservationRepository.findByApartment_IdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of(existing));

        assertThrows(ReservationConflictException.class, () -> reservationService.createReservation(dto));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_ShouldAllow_WhenNewCheckInAfterGapDays() {
        // Existing: Jul 1 – Jul 7. gapDays=1 → effectiveEnd=Jul 8. New checkIn=Jul 8 → allowed
        Apartment aptWithGap = new Apartment(1L, "Apt 1", "Short", "Desc",
                "Гагаринское плато 5/2", 1000, true, true, 3, 60, true, 4, 1,
                false, null, null, false, null, null, new ArrayList<>());
        Reservation existing = new Reservation(2L, aptWithGap,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7),
                2L, ReservationStatus.CONFIRMED, user, "john@test.com", "ua", null);

        ReservationDTO dto = new ReservationDTO(null, 1L,
                LocalDate.of(2025, 7, 8), LocalDate.of(2025, 7, 12),
                2L, ReservationStatus.PENDING, 1L, "john@test.com", "ua", null, null);

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(aptWithGap));
        when(reservationRepository.findByApartment_IdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of(existing));
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(reservationRepository.save(any())).thenReturn(reservation);

        assertDoesNotThrow(() -> reservationService.createReservation(dto));
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_ShouldIgnore_CanceledReservations() {
        Reservation canceled = new Reservation(2L, apartment,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 10),
                2L, ReservationStatus.CANCELED, user, "john@test.com", "ua", null);

        ReservationDTO dto = new ReservationDTO(null, 1L,
                LocalDate.of(2025, 7, 3), LocalDate.of(2025, 7, 8),
                2L, ReservationStatus.PENDING, 1L, "john@test.com", "ua", null, null);

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        // Repository already filtered by status — returns empty (CANCELED excluded)
        when(reservationRepository.findByApartment_IdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of());
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(reservationRepository.save(any())).thenReturn(reservation);

        assertDoesNotThrow(() -> reservationService.createReservation(dto));
        verify(reservationRepository).save(any(Reservation.class));
    }

    // --- createReservation: minStay ---

    @Test
    void createReservation_ShouldThrowMinStayViolation_WhenStayShorterThanRuleMinStay() {
        LocalDate checkIn = LocalDate.of(2025, 8, 1);
        LocalDate checkOut = LocalDate.of(2025, 8, 2); // 1 night

        DateRangeRule rule = new DateRangeRule(1L, apartment,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 10),
                RuleStatus.OPEN, null, null, 3);

        ReservationDTO dto = new ReservationDTO(null, 1L, checkIn, checkOut,
                2L, ReservationStatus.PENDING, 1L, "john@test.com", "ua", null, null);

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(dateRangeRuleRepository.findByApartmentAndDateRange(1L, checkIn, checkOut.minusDays(1)))
                .thenReturn(List.of(rule));

        assertThrows(MinStayViolationException.class, () -> reservationService.createReservation(dto));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_ShouldAllow_WhenStayMeetsRuleMinStay() {
        LocalDate checkIn = LocalDate.of(2025, 8, 1);
        LocalDate checkOut = LocalDate.of(2025, 8, 4); // 3 nights

        DateRangeRule rule = new DateRangeRule(1L, apartment,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 10),
                RuleStatus.OPEN, null, null, 3);

        ReservationDTO dto = new ReservationDTO(null, 1L, checkIn, checkOut,
                2L, ReservationStatus.PENDING, 1L, "john@test.com", "ua", null, null);

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(dateRangeRuleRepository.findByApartmentAndDateRange(1L, checkIn, checkOut.minusDays(1)))
                .thenReturn(List.of(rule));
        when(reservationRepository.findByApartment_IdAndStatusIn(eq(1L), anyList())).thenReturn(List.of());
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(reservationRepository.save(any())).thenReturn(reservation);

        assertDoesNotThrow(() -> reservationService.createReservation(dto));
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_ShouldIgnore_RuleOutsideCheckInDate() {
        // Rule covers Aug 10-20 with minStay=5; new stay starts Aug 1 (outside the rule) — should not apply.
        LocalDate checkIn = LocalDate.of(2025, 8, 1);
        LocalDate checkOut = LocalDate.of(2025, 8, 2); // 1 night

        DateRangeRule rule = new DateRangeRule(1L, apartment,
                LocalDate.of(2025, 8, 10), LocalDate.of(2025, 8, 20),
                RuleStatus.OPEN, null, null, 5);

        ReservationDTO dto = new ReservationDTO(null, 1L, checkIn, checkOut,
                2L, ReservationStatus.PENDING, 1L, "john@test.com", "ua", null, null);

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(dateRangeRuleRepository.findByApartmentAndDateRange(1L, checkIn, checkOut.minusDays(1)))
                .thenReturn(List.of(rule));
        when(reservationRepository.findByApartment_IdAndStatusIn(eq(1L), anyList())).thenReturn(List.of());
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(reservationRepository.save(any())).thenReturn(reservation);

        assertDoesNotThrow(() -> reservationService.createReservation(dto));
        verify(reservationRepository).save(any(Reservation.class));
    }

    // --- getAllReservations ---

    @Test
    void getAllReservations_ShouldReturnMappedDTOs() {
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));

        List<ReservationDTO> result = reservationService.getAllReservations();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("john@test.com", result.get(0).getClientEmail());
    }

    // --- deleteReservation ---

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

    // --- deletePendingReservations ---

    @Test
    void deletePendingReservations_ShouldCallRepositoryDelete() {
        reservationService.deletePendingReservations();

        verify(reservationRepository).deleteByStatus(ReservationStatus.PENDING);
    }

    // --- pendingReservation ---

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

    // --- cancelReservation ---

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

    // --- confirmReservation ---

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
