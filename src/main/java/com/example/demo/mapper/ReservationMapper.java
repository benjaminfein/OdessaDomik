package com.example.demo.mapper;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.model.Apartment;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.repository.UserRepository;

public class ReservationMapper {

    public static ReservationDTO mapToReservationDTO(Reservation reservation) {
        return new ReservationDTO(
                reservation.getId(),
                reservation.getApartment().getId(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getGuestCount(),
                reservation.getStatus(),
                reservation.getUser().getId(),
                reservation.getUser().getEmail()
        );
    }

    public static Reservation mapToReservation(ReservationDTO reservationDTO, ApartmentRepository apartmentRepository, UserRepository userRepository) {
        Apartment apartment = apartmentRepository.findById(reservationDTO.getApartmentId())
                .orElseThrow(() -> new RuntimeException("Квартира с id " + reservationDTO.getApartmentId() + " не найдена"));
        User user = userRepository.findByEmail(reservationDTO.getClientEmail())
                .orElseThrow(() -> new RuntimeException("Пользователь с email " + reservationDTO.getClientEmail() + " не найден"));

        return new Reservation(
                reservationDTO.getId(),
                apartment,
                reservationDTO.getCheckInDate(),
                reservationDTO.getCheckOutDate(),
                reservationDTO.getGuestCount(),
                reservationDTO.getStatus() != null ? reservationDTO.getStatus() : ReservationStatus.PENDING,
                user,
                reservationDTO.getClientEmail()
        );
    }
}
