package com.example.demo.dto;

import com.example.demo.model.Apartment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private Long id;
    private Long apartmentId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
