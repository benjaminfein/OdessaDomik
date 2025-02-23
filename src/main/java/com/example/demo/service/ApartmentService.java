package com.example.demo.service;

import com.example.demo.dto.ApartmentDTO;

import java.time.LocalDate;
import java.util.List;

public interface ApartmentService {

    ApartmentDTO createApartment(ApartmentDTO apartmentDTO);

    ApartmentDTO getApartmentById(Long id);

    List<ApartmentDTO> getAllApartments();

    ApartmentDTO updateApartment(Long id, ApartmentDTO apartmentToUpdate);

    void deleteApartment(Long id);

    List<ApartmentDTO> findAvailableApartments(LocalDate checkIn, LocalDate checkOut, int guestCount);

    List<ApartmentDTO> findApartmentsByGuestCount(int guestCount);
}
