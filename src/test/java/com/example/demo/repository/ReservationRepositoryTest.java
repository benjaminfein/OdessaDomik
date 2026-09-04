package com.example.demo.repository;

import com.example.demo.enums.ReservationStatus;
import com.example.demo.model.Apartment;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ApartmentRepository apartmentRepository;
    @Autowired private UserRepository userRepository;

    private Apartment apartment;
    private User user;

    private static final LocalDate EXISTING_START = LocalDate.of(2025, 7, 10);
    private static final LocalDate EXISTING_END   = LocalDate.of(2025, 7, 20);

    @BeforeEach
    void setUp() {
        User u = new User();
        u.setEmail("john@test.com");
        u.setRole("client");
        u.setName("John");
        u.setPassword("pwd");
        u.setEmailConfirmed(true);
        u.setDateOfCreated(new Date());
        user = userRepository.save(u);

        apartment = apartmentRepository.save(new Apartment(null, "Apt", "Short", "Desc",
                "addr", 1000, false, false, 1, 50, false, 2, 0, false, null, null, false, null, null, new ArrayList<>()));

        reservationRepository.save(new Reservation(null, apartment, EXISTING_START, EXISTING_END,
                2L, ReservationStatus.CONFIRMED, user, "john@test.com", "ua", null));
    }

    @Test
    void findBookedApartmentIds_ShouldReturnId_WhenCheckInOverlaps() {
        List<Long> result = reservationRepository.findBookedApartmentIds(
                LocalDate.of(2025, 7, 15), LocalDate.of(2025, 7, 25));

        assertEquals(1, result.size());
        assertEquals(apartment.getId(), result.get(0));
    }

    @Test
    void findBookedApartmentIds_ShouldReturnId_WhenCheckOutOverlaps() {
        List<Long> result = reservationRepository.findBookedApartmentIds(
                LocalDate.of(2025, 7, 5), LocalDate.of(2025, 7, 15));

        assertEquals(1, result.size());
    }

    @Test
    void findBookedApartmentIds_ShouldReturnId_WhenNewRangeEncompassesExisting() {
        List<Long> result = reservationRepository.findBookedApartmentIds(
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31));

        assertEquals(1, result.size());
    }

    @Test
    void findBookedApartmentIds_ShouldReturnEmpty_WhenNoOverlap() {
        List<Long> result = reservationRepository.findBookedApartmentIds(
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 7, 5));

        assertTrue(result.isEmpty());
    }

    @Test
    void findBookedApartmentIds_ShouldReturnEmpty_WhenAfterExisting() {
        List<Long> result = reservationRepository.findBookedApartmentIds(
                LocalDate.of(2025, 7, 25), LocalDate.of(2025, 8, 5));

        assertTrue(result.isEmpty());
    }

    @Test
    void findByApartmentIdAndStatusIn_ShouldReturnConfirmedAndPending() {
        reservationRepository.save(new Reservation(null, apartment,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 5),
                2L, ReservationStatus.PENDING, user, "john@test.com", "ua", null));

        List<Reservation> result = reservationRepository.findByApartment_IdAndStatusIn(
                apartment.getId(), List.of(ReservationStatus.CONFIRMED, ReservationStatus.PENDING));

        assertEquals(2, result.size());
    }

    @Test
    void findByApartmentIdAndStatusIn_ShouldNotReturn_CanceledReservations() {
        reservationRepository.save(new Reservation(null, apartment,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 5),
                2L, ReservationStatus.CANCELED, user, "john@test.com", "ua", null));

        List<Reservation> result = reservationRepository.findByApartment_IdAndStatusIn(
                apartment.getId(), List.of(ReservationStatus.CONFIRMED, ReservationStatus.PENDING));

        assertEquals(1, result.size());
        assertEquals(ReservationStatus.CONFIRMED, result.get(0).getStatus());
    }

    @Test
    void findByApartmentIdAndStatusIn_ShouldReturnEmpty_WhenOnlyCanceled() {
        reservationRepository.deleteAll();
        reservationRepository.save(new Reservation(null, apartment,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 5),
                2L, ReservationStatus.CANCELED, user, "john@test.com", "ua", null));

        List<Reservation> result = reservationRepository.findByApartment_IdAndStatusIn(
                apartment.getId(), List.of(ReservationStatus.CONFIRMED, ReservationStatus.PENDING));

        assertTrue(result.isEmpty());
    }

    @Test
    void findByApartmentIdAndStatusIn_ShouldReturnEmpty_ForDifferentApartment() {
        Apartment other = apartmentRepository.save(new Apartment(null, "Other", "Short", "Desc",
                "addr2", 500, false, false, 2, 40, false, 2, 0, false, null, null, false, null, null, new ArrayList<>()));

        List<Reservation> result = reservationRepository.findByApartment_IdAndStatusIn(
                other.getId(), List.of(ReservationStatus.CONFIRMED, ReservationStatus.PENDING));

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteByStatus_ShouldDeleteOnlyMatchingReservations() {
        reservationRepository.save(new Reservation(null, apartment,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 5),
                2L, ReservationStatus.PENDING, user, "john@test.com", "ua", null));

        reservationRepository.deleteByStatus(ReservationStatus.PENDING);

        List<Reservation> remaining = reservationRepository.findAll();
        assertEquals(1, remaining.size());
        assertEquals(ReservationStatus.CONFIRMED, remaining.get(0).getStatus());
    }

    @Test
    void deleteByStatus_ShouldDoNothing_WhenNoMatchingStatus() {
        reservationRepository.deleteByStatus(ReservationStatus.CANCELED);

        assertEquals(1, reservationRepository.count());
    }

    @Test
    void countByUserIdAndStatusAndCreatedAtAfter_ShouldCountOnlyMatchingPendingWithinWindow() {
        reservationRepository.save(new Reservation(null, apartment,
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5),
                2L, ReservationStatus.PENDING, user, "john@test.com", "ua", null));

        long count = reservationRepository.countByUser_IdAndStatusAndCreatedAtAfter(
                user.getId(), ReservationStatus.PENDING, Instant.now().minus(1, ChronoUnit.HOURS));

        assertEquals(1, count);
    }

    @Test
    void countByUserIdAndStatusAndCreatedAtAfter_ShouldExcludeOlderThanWindow() {
        reservationRepository.save(new Reservation(null, apartment,
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5),
                2L, ReservationStatus.PENDING, user, "john@test.com", "ua", null));

        long count = reservationRepository.countByUser_IdAndStatusAndCreatedAtAfter(
                user.getId(), ReservationStatus.PENDING, Instant.now().plus(1, ChronoUnit.HOURS));

        assertEquals(0, count);
    }

    @Test
    void countByUserIdAndStatusAndCreatedAtAfter_ShouldExcludeDifferentStatus() {
        long count = reservationRepository.countByUser_IdAndStatusAndCreatedAtAfter(
                user.getId(), ReservationStatus.PENDING, Instant.now().minus(1, ChronoUnit.HOURS));

        assertEquals(0, count);
    }
}
