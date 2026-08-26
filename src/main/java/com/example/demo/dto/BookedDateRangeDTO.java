package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookedDateRangeDTO {
    private LocalDate checkIn;
    private LocalDate checkOut;
    private LocalDate effectiveCheckOut;
}
