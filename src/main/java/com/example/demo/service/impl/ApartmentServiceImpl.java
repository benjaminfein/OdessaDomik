package com.example.demo.service.impl;

import com.example.demo.dto.ApartmentDTO;
import com.example.demo.exception.ApartmentNotFoundException;
import com.example.demo.mapper.ApartmentMapper;
import com.example.demo.model.Apartment;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.service.ApartmentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ApartmentServiceImpl implements ApartmentService {
    private ApartmentRepository apartmentRepository;

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
        apartment.setName(apartmentToUpdate.getName());
        apartment.setShortDescription(apartmentToUpdate.getShortDescription());
        apartment.setDescription(apartmentToUpdate.getDescription());
        apartment.setAddress(apartmentToUpdate.getAddress());
        apartment.setPrice(apartmentToUpdate.getPrice());
        apartment.setHasParkingLot(apartmentToUpdate.getHasParkingLot());
        apartment.setHasWiFi(apartmentToUpdate.getHasWiFi());
        apartment.setFloorNumber(apartmentToUpdate.getFloorNumber());
        apartment.setAreaOfApartment(apartmentToUpdate.getAreaOfApartment());
        apartment.setHasSeaView(apartmentToUpdate.getHasSeaView());
        apartment.setCountOfSleepPlaces(apartmentToUpdate.getCountOfSleepPlaces());
        Apartment updatedApartment = apartmentRepository.save(apartment);
        return ApartmentMapper.mapToApartmentDTO(updatedApartment);
    }

    @Override
    public void deleteApartment(Long id) {
        Apartment apartment = apartmentRepository.findById(id).orElseThrow(
                () -> new ApartmentNotFoundException("Apartment is not exist with given id: " + id)
        );
        apartmentRepository.deleteById(id);
    }
}
