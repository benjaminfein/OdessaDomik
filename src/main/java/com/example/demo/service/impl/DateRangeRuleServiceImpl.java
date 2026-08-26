package com.example.demo.service.impl;

import com.example.demo.dto.DateRangeRuleDTO;
import com.example.demo.enums.RuleStatus;
import com.example.demo.exception.ApartmentNotFoundException;
import com.example.demo.mapper.DateRangeRuleMapper;
import com.example.demo.model.Apartment;
import com.example.demo.model.DateRangeRule;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.repository.DateRangeRuleRepository;
import com.example.demo.service.DateRangeRuleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class DateRangeRuleServiceImpl implements DateRangeRuleService {
    private DateRangeRuleRepository dateRangeRuleRepository;
    private ApartmentRepository apartmentRepository;

    @Override
    public DateRangeRuleDTO saveRule(Long apartmentId, DateRangeRuleDTO dto) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ApartmentNotFoundException("Apartment not found: " + apartmentId));

        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();

        if (RuleStatus.CLOSED.name().equals(dto.getStatus())) {
            // Merge with adjacent/overlapping CLOSED rules into one continuous block
            List<DateRangeRule> adjacentClosed = dateRangeRuleRepository
                    .findAdjacentClosedRules(apartmentId, startDate.minusDays(1), endDate.plusDays(1));

            for (DateRangeRule existing : adjacentClosed) {
                if (existing.getStartDate().isBefore(startDate)) startDate = existing.getStartDate();
                if (existing.getEndDate().isAfter(endDate)) endDate = existing.getEndDate();
            }
            if (!adjacentClosed.isEmpty()) {
                List<Long> ids = adjacentClosed.stream().map(DateRangeRule::getId).collect(Collectors.toList());
                dateRangeRuleRepository.deleteByIds(ids);
            }
        }

        // Whatever is being saved (OPEN or CLOSED) must be the sole authority over its date
        // range: trim/remove ANY existing rule of ANY status that overlaps it, keeping the
        // parts outside the new range intact. Without this, saving a second OPEN rule (e.g.
        // a new price) over the same dates as an earlier OPEN rule left both rows in the DB,
        // and which one "won" in ReservationServiceImpl's price/minStay lookup depended on
        // unspecified DB row order instead of on which was saved last.
        replaceOverlapping(apartmentId, startDate, endDate);

        DateRangeRule rule = DateRangeRuleMapper.toEntity(dto, apartment);
        rule.setStartDate(startDate);
        rule.setEndDate(endDate);
        return DateRangeRuleMapper.toDTO(dateRangeRuleRepository.save(rule));
    }

    private void replaceOverlapping(Long apartmentId, LocalDate startDate, LocalDate endDate) {
        List<DateRangeRule> overlapping = dateRangeRuleRepository
                .findByApartmentAndDateRange(apartmentId, startDate, endDate);

        List<Long> toDelete = new ArrayList<>();
        List<DateRangeRule> toCreate = new ArrayList<>();

        for (DateRangeRule existing : overlapping) {
            toDelete.add(existing.getId());

            if (existing.getStartDate().isBefore(startDate)) {
                // Left remnant: [existing.start … newStart-1], keeps existing rule's own status/price/minStay
                toCreate.add(cloneWithRange(existing, existing.getStartDate(), startDate.minusDays(1)));
            }
            if (existing.getEndDate().isAfter(endDate)) {
                // Right remnant: [newEnd+1 … existing.end]
                toCreate.add(cloneWithRange(existing, endDate.plusDays(1), existing.getEndDate()));
            }
        }

        if (!toDelete.isEmpty()) {
            dateRangeRuleRepository.deleteByIds(toDelete);
        }
        if (!toCreate.isEmpty()) {
            dateRangeRuleRepository.saveAll(toCreate);
        }
    }

    private DateRangeRule cloneWithRange(DateRangeRule source, LocalDate start, LocalDate end) {
        DateRangeRule r = new DateRangeRule();
        r.setApartment(source.getApartment());
        r.setStartDate(start);
        r.setEndDate(end);
        r.setStatus(source.getStatus());
        r.setPriceOverride(source.getPriceOverride());
        r.setPriceUnit(source.getPriceUnit());
        r.setMinStay(source.getMinStay());
        return r;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DateRangeRuleDTO> getRules(Long apartmentId, LocalDate from, LocalDate to) {
        return dateRangeRuleRepository
                .findByApartmentAndDateRange(apartmentId, from, to)
                .stream()
                .map(DateRangeRuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRule(Long ruleId) {
        dateRangeRuleRepository.deleteById(ruleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findClosedApartmentIds(LocalDate startDate, LocalDate endDate) {
        return dateRangeRuleRepository.findClosedApartmentIds(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findApartmentIdsWithInsufficientStay(LocalDate checkIn, int nights) {
        return dateRangeRuleRepository.findApartmentIdsWithInsufficientStay(checkIn, nights);
    }

    // Mirrors the rule-matching used as a safety net in
    // ReservationServiceImpl.validateMinStay: finds the OPEN rule covering
    // checkIn and returns its minStay, or null if none applies. Deliberately
    // returns only this single number — never the full rule (price overrides
    // etc. stay admin-only) — since this backs a public-facing endpoint.
    @Override
    @Transactional(readOnly = true)
    public Integer getEffectiveMinStay(Long apartmentId, LocalDate checkIn) {
        return dateRangeRuleRepository
                .findByApartmentAndDateRange(apartmentId, checkIn, checkIn)
                .stream()
                .filter(r -> r.getStatus() == RuleStatus.OPEN && r.getMinStay() != null)
                .findFirst()
                .map(DateRangeRule::getMinStay)
                .orElse(null);
    }
}
