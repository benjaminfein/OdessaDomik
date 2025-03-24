package com.example.demo.service;

import com.example.demo.dto.ReservationDTO;
import jakarta.mail.MessagingException;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    List<Long> findBookedApartmentIds(LocalDate startDate, LocalDate endDate);

    ReservationDTO createReservation(ReservationDTO reservationDTO);

    List<ReservationDTO> getAllReservations();

    void deleteReservation(Long id);

    void deletePendingReservations();

    void pendingReservation(Long reservationId) throws MessagingException;

    void cancelReservation(Long reservationId) throws MessagingException;

    void confirmReservation(Long reservationId) throws MessagingException;
}