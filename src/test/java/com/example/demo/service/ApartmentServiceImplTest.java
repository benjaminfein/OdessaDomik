package com.example.demo.service;

import com.example.demo.dto.ApartmentDTO;
import com.example.demo.exception.ApartmentNotFoundException;
import com.example.demo.mapper.ApartmentMapper;
import com.example.demo.model.Apartment;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.service.impl.ApartmentServiceImpl;
import com.example.demo.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApartmentServiceImplTest {
    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private ReservationServiceImpl reservationService;

    @InjectMocks
    private ApartmentServiceImpl apartmentService;

    @Test
    void createApartment_ShouldSaveAndReturnDTO() {
        ApartmentDTO dto = new ApartmentDTO();
        dto.setName("Test");

        Apartment entity = ApartmentMapper.mapToApartment(dto);
        Apartment savedEntity = new Apartment(
                1L,
                "apartment",
                "shortDescription",
                "description",
                "address",
                123123,
                true,
                true,
                5,
                56,
                false,
                6,
                new ArrayList<>()
        );

        when(apartmentRepository.save(any(Apartment.class))).thenReturn(savedEntity);

        ApartmentDTO result = apartmentService.createApartment(dto);

        assertEquals("apartment", result.getName());
        verify(apartmentRepository, times(1)).save(any(Apartment.class));
    }

    @Test
    void getApartmentById_ShouldReturnApartmentDTO() {
        Apartment entity = new Apartment();
        entity.setId(1L);
        entity.setName("apartment");

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        ApartmentDTO result = apartmentService.getApartmentById(1L);

        assertEquals("apartment", result.getName());
        verify(apartmentRepository, times(1)).findById(1L);
    }

    @Test
    void getApartmentById_ShouldThrow_WhenNotFound() {
        when(apartmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ApartmentNotFoundException.class,
                () -> apartmentService.getApartmentById(1L)
        );
    }

    @Test
    void getAllApartments_ShouldReturnListOfApartmentDTO() {
        Apartment firstEntity = new Apartment();
        firstEntity.setId(1L);
        firstEntity.setName("first apartment");

        Apartment secondEntity = new Apartment();
        secondEntity.setId(2L);
        secondEntity.setName("second apartment");

        when(apartmentRepository.findAll()).thenReturn(List.of(firstEntity, secondEntity));

        List<ApartmentDTO> result = apartmentService.getAllApartments();

        assertEquals(2, result.size());
        assertEquals("first apartment", result.get(0).getName());
        assertEquals("second apartment", result.get(1).getName());

        verify(apartmentRepository, times(1)).findAll();
    }

    @Test
    void updateApartment_ShouldSaveAndReturnDTO_WhenEntitiesAreDifferent() {
        Apartment entity1 = new Apartment();
        entity1.setId(1L);
        entity1.setName("a");

        ApartmentDTO entity2 = new ApartmentDTO();
        entity2.setId(1L);
        entity2.setName("b");

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(entity1));

        entity1 = ApartmentMapper.mapToApartment(entity2);

        when(apartmentRepository.save(any(Apartment.class))).thenReturn(entity1);

        ApartmentDTO result = apartmentService.updateApartment(1L, entity2);

        assertEquals("b", result.getName());
    }

    @Test
    void updateApartment_ShouldNotSave_WhenEntitiesAreEqual() {
        Apartment existing = new Apartment();
        existing.setId(1L);
        existing.setName("a");

        ApartmentDTO dto = new ApartmentDTO();
        dto.setId(1L);
        dto.setName("a");

        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(existing));

        ApartmentDTO result = apartmentService.updateApartment(1L, dto);

        verify(apartmentRepository, never()).save(any());
        assertEquals("a", result.getName());
        assertEquals(1L, result.getId());
    }

    @Test
    void updateApartment_ShouldThrow_WhenNotFound() {
        ApartmentDTO dto = new ApartmentDTO();
        when(apartmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ApartmentNotFoundException.class,
                () -> apartmentService.updateApartment(1L, dto)
        );

        verify(apartmentRepository).findById(1L);
        verify(apartmentRepository, never()).save(any());
    }

    @Test
    void deleteApartment_ShouldDelete_WhenExists() {
        Apartment entity = new Apartment();
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        apartmentService.deleteApartment(1L);

        verify(apartmentRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteApartment_ShouldThrow_WhenNotFound() {
        when(apartmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ApartmentNotFoundException.class,
                () -> apartmentService.deleteApartment(1L)
        );

        verify(apartmentRepository, never()).deleteById(anyLong());
    }

    @Test
    void findAvailableApartments_ShouldFoundApartmentsOnlyByGuestCount() {
        Apartment firstApartment = new Apartment(
                1L,
                "apartment1",
                "shortDescription",
                "description",
                "address2",
                123123,
                true,
                true,
                5,
                56,
                false,
                6,
                new ArrayList<>()
        );

        Apartment secondApartment = new Apartment(
                2L,
                "apartment2",
                "shortDescription",
                "description",
                "address2",
                321321,
                false,
                true,
                7,
                56,
                false,
                4,
                new ArrayList<>()
        );

        when(apartmentRepository.findByCountOfSleepPlacesGreaterThanEqual(5))
                .thenReturn(List.of(firstApartment, secondApartment));

        List<ApartmentDTO> result = apartmentService.findApartmentsByGuestCount(5);

        assertEquals(2, result.size());
        assertEquals(firstApartment.getName(), result.get(0).getName());
        assertEquals(secondApartment.getName(), result.get(1).getName());
        verify(apartmentRepository, times(1))
                .findByCountOfSleepPlacesGreaterThanEqual(5);
    }
}
