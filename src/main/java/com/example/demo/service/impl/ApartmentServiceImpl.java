package com.example.demo.service.impl;

import com.example.demo.dto.ApartmentDTO;
import com.example.demo.exception.ApartmentNotFoundException;
import com.example.demo.mapper.ApartmentMapper;
import com.example.demo.model.Apartment;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ApartmentService;
import com.example.demo.service.DateRangeRuleService;
import com.example.demo.service.ReservationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ApartmentServiceImpl implements ApartmentService {
    private ApartmentRepository apartmentRepository;
    private ReservationService reservationService;
    private DateRangeRuleService dateRangeRuleService;

    @Override
    public ApartmentDTO createApartment(ApartmentDTO apartmentDTO) {
        Apartment apartment = ApartmentMapper.mapToApartment(apartmentDTO);
        Apartment savedApartment = apartmentRepository.save(apartment);
        return ApartmentMapper.mapToApartmentDTO(savedApartment);
    }

    @Override
    public ApartmentDTO getApartmentById(Long id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() ->
                        new ApartmentNotFoundException("Apartment is not exist with given id: " + id));

        return ApartmentMapper.mapToApartmentDTO(apartment);
    }

    @Override
    public List<ApartmentDTO> getAllApartments() {
        List<Apartment> apartments = apartmentRepository.findAll();
        return apartments.stream().map(ApartmentMapper::mapToApartmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ApartmentDTO updateApartment(Long id, ApartmentDTO apartmentToUpdate) {
        Apartment apartment = apartmentRepository.findById(id).orElseThrow(
                () -> new ApartmentNotFoundException("Apartment is not exist with given id: " + id)
        );
        Apartment apartmentToSave = ApartmentMapper.mapToApartment(apartmentToUpdate);
        apartmentToSave.setId(id);
        if (!apartment.equals(apartmentToSave)) {
            apartmentRepository.save(apartmentToSave);
        }
        return ApartmentMapper.mapToApartmentDTO(apartmentToSave);
    }

    @Override
    public void deleteApartment(Long id) {
        Apartment apartment = apartmentRepository.findById(id).orElseThrow(
                () -> new ApartmentNotFoundException("Apartment is not exist with given id: " + id)
        );
        apartmentRepository.deleteById(id);
    }

    @Override
    public List<ApartmentDTO> findAvailableApartments(LocalDate startDate, LocalDate endDate, int guestCount) {
        List<Apartment> availableApartments;

        if (startDate == null || endDate == null) {
            availableApartments = apartmentRepository.findByCountOfSleepPlacesGreaterThanEqual(guestCount);
        } else {
            int nights = (int) ChronoUnit.DAYS.between(startDate, endDate);

            Set<Long> excludedIds = new HashSet<>();
            excludedIds.addAll(reservationService.findBookedApartmentIds(startDate, endDate));
            excludedIds.addAll(dateRangeRuleService.findClosedApartmentIds(startDate, endDate));
            excludedIds.addAll(dateRangeRuleService.findApartmentIdsWithInsufficientStay(startDate, nights));

            availableApartments = excludedIds.isEmpty()
                    ? apartmentRepository.findByCountOfSleepPlacesGreaterThanEqual(guestCount)
                    : apartmentRepository
                    .findByIdNotInAndCountOfSleepPlacesGreaterThanEqual(new ArrayList<>(excludedIds), guestCount);
        }

        return availableApartments.stream().map(ApartmentMapper::mapToApartmentDTO).toList();
    }

    @Override
    public List<ApartmentDTO> findApartmentsByGuestCount(int guestCount) {
        List<Apartment> apartments = apartmentRepository.findByCountOfSleepPlacesGreaterThanEqual(guestCount);
        return apartments.stream().map(ApartmentMapper::mapToApartmentDTO).toList();
    }
}
