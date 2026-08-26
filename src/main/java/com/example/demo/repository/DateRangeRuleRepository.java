package com.example.demo.repository;

import com.example.demo.model.DateRangeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DateRangeRuleRepository extends JpaRepository<DateRangeRule, Long> {

    @Query("SELECT r FROM DateRangeRule r WHERE r.apartment.id = :apartmentId " +
            "AND r.status = 'CLOSED' " +
            "AND r.startDate <= :endDate AND r.endDate >= :startDate")
    List<DateRangeRule> findAdjacentClosedRules(
            @Param("apartmentId") Long apartmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DateRangeRule r WHERE r.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);

    @Query("SELECT r FROM DateRangeRule r WHERE r.apartment.id = :apartmentId " +
            "AND r.startDate <= :endDate AND r.endDate >= :startDate")
    List<DateRangeRule> findByApartmentAndDateRange(
            @Param("apartmentId") Long apartmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT r.apartment.id FROM DateRangeRule r " +
            "WHERE r.status = 'CLOSED' " +
            "AND r.startDate <= :endDate AND r.endDate >= :startDate")
    List<Long> findClosedApartmentIds(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT r.apartment.id FROM DateRangeRule r " +
            "WHERE r.status = 'OPEN' AND r.minStay IS NOT NULL AND r.minStay > :nights " +
            "AND r.startDate <= :checkIn AND r.endDate >= :checkIn")
    List<Long> findApartmentIdsWithInsufficientStay(
            @Param("checkIn") LocalDate checkIn,
            @Param("nights") int nights);
}
