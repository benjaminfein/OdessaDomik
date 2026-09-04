package com.example.demo.repository;

import com.example.demo.enums.ReservationStatus;
import com.example.demo.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    List<Reservation> findByApartment_IdAndStatusIn(Long apartmentId, List<ReservationStatus> statuses);

    @Query("SELECT r FROM Reservation r WHERE r.apartment.id = :apartmentId " +
           "AND r.checkInDate <= :to AND r.checkOutDate >= :from " +
           "AND r.status IN :statuses")
    List<Reservation> findByApartmentAndDateRange(
            @Param("apartmentId") Long apartmentId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("statuses") List<ReservationStatus> statuses);

    void deleteByStatus(ReservationStatus status);

    long countByUser_IdAndStatusAndCreatedAtAfter(Long userId, ReservationStatus status, Instant after);
}