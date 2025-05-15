package com.example.demo.repository;

import com.example.demo.model.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    List<Apartment> findByCountOfSleepPlacesGreaterThanEqual(int guestCount);

    List<Apartment> findByIdNotInAndCountOfSleepPlacesGreaterThanEqual(List<Long> bookedApartmentIds, int guestCount);
}
