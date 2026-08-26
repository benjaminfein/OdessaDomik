package com.example.demo.mapper;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.exception.ApartmentNotFoundException;
import com.example.demo.exception.UserNotFoundException;
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
                reservation.getUser().getEmail(),
                reservation.getClientLang(),
                reservation.getTotalPrice(),
                reservation.getUser().getName()
        );
    }

    public static Reservation mapToReservation(ReservationDTO reservationDTO, ApartmentRepository apartmentRepository,
                                               UserRepository userRepository) {
        Apartment apartment = apartmentRepository.findById(reservationDTO.getApartmentId())
                .orElseThrow(() -> new ApartmentNotFoundException("There is no apartment with following id: "
                        + reservationDTO.getApartmentId()));
        User user = userRepository.findByEmail(reservationDTO.getClientEmail())
                .orElseThrow(() -> new UserNotFoundException("There is no user with following id: "
                        + reservationDTO.getClientEmail()));

        return new Reservation(
                reservationDTO.getId(),
                apartment,
                reservationDTO.getCheckInDate(),
                reservationDTO.getCheckOutDate(),
                reservationDTO.getGuestCount(),
                reservationDTO.getStatus() != null ? reservationDTO.getStatus() : ReservationStatus.PENDING,
                user,
                reservationDTO.getClientEmail(),
                reservationDTO.getClientLang(),
                null  // totalPrice рассчитывается в сервисе после маппинга
        );
    }
}
