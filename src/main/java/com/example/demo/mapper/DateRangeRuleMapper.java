package com.example.demo.mapper;

import com.example.demo.dto.DateRangeRuleDTO;
import com.example.demo.enums.PriceUnit;
import com.example.demo.enums.RuleStatus;
import com.example.demo.model.Apartment;
import com.example.demo.model.DateRangeRule;

public class DateRangeRuleMapper {

    public static DateRangeRuleDTO toDTO(DateRangeRule rule) {
        DateRangeRuleDTO dto = new DateRangeRuleDTO();
        dto.setId(rule.getId());
        dto.setApartmentId(rule.getApartment().getId());
        dto.setStartDate(rule.getStartDate());
        dto.setEndDate(rule.getEndDate());
        dto.setStatus(rule.getStatus().name());
        dto.setPriceOverride(rule.getPriceOverride());
        dto.setPriceUnit(rule.getPriceUnit() != null ? rule.getPriceUnit().name() : null);
        dto.setMinStay(rule.getMinStay());
        return dto;
    }

    public static DateRangeRule toEntity(DateRangeRuleDTO dto, Apartment apartment) {
        DateRangeRule rule = new DateRangeRule();
        rule.setApartment(apartment);
        rule.setStartDate(dto.getStartDate());
        rule.setEndDate(dto.getEndDate());
        rule.setStatus(RuleStatus.valueOf(dto.getStatus()));
        rule.setPriceOverride(dto.getPriceOverride());
        rule.setPriceUnit(dto.getPriceUnit() != null ? PriceUnit.valueOf(dto.getPriceUnit()) : null);
        rule.setMinStay(dto.getMinStay());
        return rule;
    }
}
