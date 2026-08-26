package com.example.demo.service;

import com.example.demo.dto.DateRangeRuleDTO;

import java.time.LocalDate;
import java.util.List;

public interface DateRangeRuleService {
    DateRangeRuleDTO saveRule(Long apartmentId, DateRangeRuleDTO dto);

    List<DateRangeRuleDTO> getRules(Long apartmentId, LocalDate from, LocalDate to);

    void deleteRule(Long ruleId);

    List<Long> findClosedApartmentIds(LocalDate startDate, LocalDate endDate);

    List<Long> findApartmentIdsWithInsufficientStay(LocalDate checkIn, int nights);

    Integer getEffectiveMinStay(Long apartmentId, LocalDate checkIn);
}
