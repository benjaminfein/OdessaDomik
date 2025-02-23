package com.example.demo.service;

import com.example.demo.dto.ReservationDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    List<Long> findBookedApartmentIds(LocalDate startDate, LocalDate endDate);

    ReservationDTO createReservation(ReservationDTO reservationDTO);

    List<ReservationDTO> getAllReservations();
}