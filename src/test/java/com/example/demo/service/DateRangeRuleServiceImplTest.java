package com.example.demo.service;

import com.example.demo.dto.DateRangeRuleDTO;
import com.example.demo.enums.PriceUnit;
import com.example.demo.enums.RuleStatus;
import com.example.demo.exception.ApartmentNotFoundException;
import com.example.demo.model.Apartment;
import com.example.demo.model.DateRangeRule;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.repository.DateRangeRuleRepository;
import com.example.demo.service.impl.DateRangeRuleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DateRangeRuleServiceImplTest {

    @Mock
    private DateRangeRuleRepository dateRangeRuleRepository;

    @Mock
    private ApartmentRepository apartmentRepository;

    @InjectMocks
    private DateRangeRuleServiceImpl dateRangeRuleService;

    private Apartment apartment() {
        Apartment apartment = new Apartment();
        apartment.setId(1L);
        return apartment;
    }

    private DateRangeRule rule(Long id, LocalDate start, LocalDate end, RuleStatus status) {
        DateRangeRule rule = new DateRangeRule();
        rule.setId(id);
        rule.setApartment(apartment());
        rule.setStartDate(start);
        rule.setEndDate(end);
        rule.setStatus(status);
        return rule;
    }

    private DateRangeRuleDTO dto(LocalDate start, LocalDate end, RuleStatus status) {
        DateRangeRuleDTO dto = new DateRangeRuleDTO();
        dto.setStartDate(start);
        dto.setEndDate(end);
        dto.setStatus(status.name());
        dto.setPriceOverride(1000);
        dto.setPriceUnit(PriceUnit.UAH.name());
        dto.setMinStay(2);
        return dto;
    }

    @Test
    void saveRule_ShouldThrow_WhenApartmentNotFound() {
        when(apartmentRepository.findById(99L)).thenReturn(Optional.empty());

        DateRangeRuleDTO dto = dto(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7), RuleStatus.OPEN);

        assertThrows(ApartmentNotFoundException.class, () -> dateRangeRuleService.saveRule(99L, dto));
        verify(dateRangeRuleRepository, never()).save(any());
    }

    @Test
    void saveRule_ShouldSaveOpenRule_WhenNoOverlaps() {
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment()));
        when(dateRangeRuleRepository.findByApartmentAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of());

        DateRangeRuleDTO dto = dto(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7), RuleStatus.OPEN);
        DateRangeRule saved = rule(1L, dto.getStartDate(), dto.getEndDate(), RuleStatus.OPEN);
        saved.setPriceOverride(1000);
        saved.setPriceUnit(PriceUnit.UAH);
        saved.setMinStay(2);
        when(dateRangeRuleRepository.save(any())).thenReturn(saved);

        DateRangeRuleDTO result = dateRangeRuleService.saveRule(1L, dto);

        assertEquals(RuleStatus.OPEN.name(), result.getStatus());
        assertEquals(1000, result.getPriceOverride());
        verify(dateRangeRuleRepository).save(any());
        verify(dateRangeRuleRepository, never()).deleteByIds(anyList());
    }

    @Test
    void saveRule_ShouldMergeAdjacentClosedRules_WhenStatusClosed() {
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment()));

        LocalDate start = LocalDate.of(2025, 7, 10);
        LocalDate end = LocalDate.of(2025, 7, 15);
        DateRangeRule adjacent = rule(5L, LocalDate.of(2025, 7, 5), LocalDate.of(2025, 7, 9), RuleStatus.CLOSED);
        when(dateRangeRuleRepository.findAdjacentClosedRules(eq(1L), eq(start.minusDays(1)), eq(end.plusDays(1))))
                .thenReturn(List.of(adjacent));
        when(dateRangeRuleRepository.findByApartmentAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of());

        DateRangeRuleDTO dto = dto(start, end, RuleStatus.CLOSED);
        when(dateRangeRuleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        dateRangeRuleService.saveRule(1L, dto);

        verify(dateRangeRuleRepository).deleteByIds(List.of(5L));

        ArgumentCaptor<DateRangeRule> captor = ArgumentCaptor.forClass(DateRangeRule.class);
        verify(dateRangeRuleRepository).save(captor.capture());
        assertEquals(LocalDate.of(2025, 7, 5), captor.getValue().getStartDate());
        assertEquals(end, captor.getValue().getEndDate());
    }

    @Test
    void saveRule_ShouldTrimOverlappingRule_WhenNewRangeIsInTheMiddle() {
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment()));

        LocalDate newStart = LocalDate.of(2025, 7, 10);
        LocalDate newEnd = LocalDate.of(2025, 7, 15);
        DateRangeRule existing = rule(3L, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 20), RuleStatus.OPEN);
        when(dateRangeRuleRepository.findByApartmentAndDateRange(1L, newStart, newEnd))
                .thenReturn(List.of(existing));
        when(dateRangeRuleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DateRangeRuleDTO dto = dto(newStart, newEnd, RuleStatus.OPEN);
        dateRangeRuleService.saveRule(1L, dto);

        verify(dateRangeRuleRepository).deleteByIds(List.of(3L));

        ArgumentCaptor<List<DateRangeRule>> captor = ArgumentCaptor.forClass(List.class);
        verify(dateRangeRuleRepository).saveAll(captor.capture());
        List<DateRangeRule> remnants = captor.getValue();
        assertEquals(2, remnants.size());
        assertEquals(LocalDate.of(2025, 7, 1), remnants.get(0).getStartDate());
        assertEquals(LocalDate.of(2025, 7, 9), remnants.get(0).getEndDate());
        assertEquals(LocalDate.of(2025, 7, 16), remnants.get(1).getStartDate());
        assertEquals(LocalDate.of(2025, 7, 20), remnants.get(1).getEndDate());
    }

    @Test
    void saveRule_ShouldNotCreateRemnants_WhenExistingRuleFullyCovered() {
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment()));

        LocalDate newStart = LocalDate.of(2025, 7, 1);
        LocalDate newEnd = LocalDate.of(2025, 7, 20);
        DateRangeRule existing = rule(3L, LocalDate.of(2025, 7, 5), LocalDate.of(2025, 7, 10), RuleStatus.OPEN);
        when(dateRangeRuleRepository.findByApartmentAndDateRange(1L, newStart, newEnd))
                .thenReturn(List.of(existing));
        when(dateRangeRuleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DateRangeRuleDTO dto = dto(newStart, newEnd, RuleStatus.OPEN);
        dateRangeRuleService.saveRule(1L, dto);

        verify(dateRangeRuleRepository).deleteByIds(List.of(3L));
        verify(dateRangeRuleRepository, never()).saveAll(anyList());
    }

    @Test
    void getRules_ShouldReturnMappedDTOs() {
        DateRangeRule r1 = rule(1L, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 7), RuleStatus.OPEN);
        when(dateRangeRuleRepository.findByApartmentAndDateRange(1L,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31)))
                .thenReturn(List.of(r1));

        List<DateRangeRuleDTO> result = dateRangeRuleService.getRules(
                1L, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31));

        assertEquals(1, result.size());
        assertEquals(RuleStatus.OPEN.name(), result.get(0).getStatus());
    }

    @Test
    void deleteRule_ShouldCallRepository() {
        dateRangeRuleService.deleteRule(7L);

        verify(dateRangeRuleRepository).deleteById(7L);
    }

    @Test
    void findClosedApartmentIds_ShouldDelegateToRepository() {
        LocalDate from = LocalDate.of(2025, 7, 1);
        LocalDate to = LocalDate.of(2025, 7, 31);
        when(dateRangeRuleRepository.findClosedApartmentIds(from, to)).thenReturn(List.of(1L, 2L));

        List<Long> result = dateRangeRuleService.findClosedApartmentIds(from, to);

        assertEquals(List.of(1L, 2L), result);
    }

    @Test
    void findApartmentIdsWithInsufficientStay_ShouldDelegateToRepository() {
        LocalDate checkIn = LocalDate.of(2025, 7, 1);
        when(dateRangeRuleRepository.findApartmentIdsWithInsufficientStay(checkIn, 2))
                .thenReturn(List.of(3L));

        List<Long> result = dateRangeRuleService.findApartmentIdsWithInsufficientStay(checkIn, 2);

        assertEquals(List.of(3L), result);
    }

    @Test
    void getEffectiveMinStay_ShouldReturnMinStay_WhenOpenRuleCoversDate() {
        LocalDate checkIn = LocalDate.of(2025, 7, 5);
        DateRangeRule openRule = rule(1L, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 10), RuleStatus.OPEN);
        openRule.setMinStay(4);
        when(dateRangeRuleRepository.findByApartmentAndDateRange(1L, checkIn, checkIn))
                .thenReturn(List.of(openRule));

        Integer result = dateRangeRuleService.getEffectiveMinStay(1L, checkIn);

        assertEquals(4, result);
    }

    @Test
    void getEffectiveMinStay_ShouldReturnNull_WhenNoMatchingOpenRule() {
        LocalDate checkIn = LocalDate.of(2025, 7, 5);
        DateRangeRule closedRule = rule(1L, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 10), RuleStatus.CLOSED);
        closedRule.setMinStay(4);
        when(dateRangeRuleRepository.findByApartmentAndDateRange(1L, checkIn, checkIn))
                .thenReturn(List.of(closedRule));

        Integer result = dateRangeRuleService.getEffectiveMinStay(1L, checkIn);

        assertNull(result);
    }

    @Test
    void getEffectiveMinStay_ShouldReturnNull_WhenNoRulesCoverDate() {
        LocalDate checkIn = LocalDate.of(2025, 7, 5);
        when(dateRangeRuleRepository.findByApartmentAndDateRange(1L, checkIn, checkIn))
                .thenReturn(List.of());

        Integer result = dateRangeRuleService.getEffectiveMinStay(1L, checkIn);

        assertNull(result);
    }
}
