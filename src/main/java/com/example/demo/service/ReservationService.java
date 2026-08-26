package com.example.demo.service;

import com.example.demo.dto.BookedDateRangeDTO;
import com.example.demo.dto.ReservationDTO;
import jakarta.mail.MessagingException;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    List<Long> findBookedApartmentIds(LocalDate startDate, LocalDate endDate);

    List<BookedDateRangeDTO> getBookedDateRanges(Long apartmentId);

    ReservationDTO createReservation(ReservationDTO reservationDTO);

    int calculatePrice(Long apartmentId, LocalDate checkIn, LocalDate checkOut, Long guestCount);

    List<ReservationDTO> getAllReservations();

    List<ReservationDTO> getReservationsByApartmentAndDateRange(Long apartmentId, LocalDate from, LocalDate to);

    void deleteReservation(Long id);

    void deletePendingReservations();

    void pendingReservation(Long reservationId) throws MessagingException;

    void cancelReservation(Long reservationId) throws MessagingException;

    void confirmReservation(Long reservationId) throws MessagingException;
}