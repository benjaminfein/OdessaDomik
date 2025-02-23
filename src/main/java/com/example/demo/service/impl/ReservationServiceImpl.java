package com.example.demo.service.impl;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.mapper.ApartmentMapper;
import com.example.demo.mapper.ReservationMapper;
import com.example.demo.model.Apartment;
import com.example.demo.model.Reservation;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.service.ReservationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final ApartmentRepository apartmentRepository;

    @Override
    public List<Long> findBookedApartmentIds(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findBookedApartmentIds(startDate, endDate);
    }

    @Override
    public ReservationDTO createReservation(ReservationDTO reservationDTO) {
        Reservation reservation = ReservationMapper.mapToReservation(reservationDTO, apartmentRepository);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationMapper.mapToReservationDTO(savedReservation);
    }

    @Override
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream().map(ReservationMapper::mapToReservationDTO)
                .collect(Collectors.toList());
    }
}
