package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateRangeRuleDTO {
    private Long id;
    private Long apartmentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer priceOverride;
    private String priceUnit;
    private Integer minStay;
}
