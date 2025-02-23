package com.example.demo.mapper;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.model.Apartment;
import com.example.demo.model.Reservation;
import com.example.demo.repository.ApartmentRepository;

public class ReservationMapper {

    public static ReservationDTO mapToReservationDTO(Reservation reservation) {
        return new ReservationDTO(
                reservation.getId(),
                reservation.getApartment().getId(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );
    }

    public static Reservation mapToReservation(ReservationDTO reservationDTO, ApartmentRepository apartmentRepository) {
        Apartment apartment = apartmentRepository.findById(reservationDTO.getApartmentId())
                .orElseThrow(() -> new RuntimeException("Квартира с id " + reservationDTO.getApartmentId() + " не найдена"));

        return new Reservation(
                reservationDTO.getId(),
                apartment,
                reservationDTO.getCheckInDate(),
                reservationDTO.getCheckOutDate()
        );
    }
}
