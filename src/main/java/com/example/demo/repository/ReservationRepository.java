package com.example.demo.repository;

import com.example.demo.enums.ReservationStatus;
import com.example.demo.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
        SELECT r.apartment.id FROM Reservation r
        WHERE (:checkInDate BETWEEN r.checkInDate AND r.checkOutDate)\s
        OR (:checkOutDate BETWEEN r.checkInDate AND r.checkOutDate)\s
        OR (r.checkInDate BETWEEN :checkInDate AND :checkOutDate)
        """)
    List<Long> findBookedApartmentIds(@Param("checkInDate") LocalDate checkInDate, @Param("checkOutDate") LocalDate checkOutDate);

    void deleteByStatus(ReservationStatus status);
}