package com.example.demo.repository;

import com.example.demo.model.Apartment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ApartmentRepositoryTest {

    @Autowired
    private ApartmentRepository apartmentRepository;

    private Apartment apt2;
    private Apartment apt4;
    private Apartment apt6;

    @BeforeEach
    void setUp() {
        apt2 = apartmentRepository.save(new Apartment(null, "Studio", "Short", "Desc",
                "addr1", 500, false, true, 2, 30, false, 2, new ArrayList<>()));
        apt4 = apartmentRepository.save(new Apartment(null, "Two-bedroom", "Short", "Desc",
                "addr2", 1000, true, true, 5, 70, false, 4, new ArrayList<>()));
        apt6 = apartmentRepository.save(new Apartment(null, "Penthouse", "Short", "Desc",
                "addr3", 2000, true, true, 10, 120, true, 6, new ArrayList<>()));
    }

    @Test
    void findByCountOfSleepPlacesGreaterThanEqual_ShouldReturnMatchingApartments() {
        List<Apartment> result = apartmentRepository.findByCountOfSleepPlacesGreaterThanEqual(4);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a -> a.getCountOfSleepPlaces() >= 4));
    }

    @Test
    void findByCountOfSleepPlacesGreaterThanEqual_ShouldReturnAll_WhenCountIs1() {
        List<Apartment> result = apartmentRepository.findByCountOfSleepPlacesGreaterThanEqual(1);

        assertEquals(3, result.size());
    }

    @Test
    void findByCountOfSleepPlacesGreaterThanEqual_ShouldReturnEmpty_WhenCountTooHigh() {
        List<Apartment> result = apartmentRepository.findByCountOfSleepPlacesGreaterThanEqual(7);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdNotInAndCountOfSleepPlacesGreaterThanEqual_ShouldExcludeSpecifiedIds() {
        List<Long> excluded = List.of(apt4.getId());

        List<Apartment> result = apartmentRepository
                .findByIdNotInAndCountOfSleepPlacesGreaterThanEqual(excluded, 4);

        assertEquals(1, result.size());
        assertEquals("Penthouse", result.get(0).getName());
    }

    @Test
    void findByIdNotInAndCountOfSleepPlacesGreaterThanEqual_ShouldReturnEmpty_WhenAllExcluded() {
        List<Long> excluded = List.of(apt4.getId(), apt6.getId());

        List<Apartment> result = apartmentRepository
                .findByIdNotInAndCountOfSleepPlacesGreaterThanEqual(excluded, 4);

        assertTrue(result.isEmpty());
    }
}
