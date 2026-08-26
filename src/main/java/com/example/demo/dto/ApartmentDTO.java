package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentDTO {
    private Long id;
    private String name;
    private String shortDescription;
    private String description;
    private String address;
    private Integer price;
    private Boolean hasParkingLot = false;
    private Boolean hasWiFi = false;
    private Integer floorNumber;
    private Integer areaOfApartment;
    private Boolean hasSeaView = false;
    private Integer countOfSleepPlaces;
    private Integer gapDays = 0;
    private Boolean guestPriceAboveEnabled = false;
    private Integer guestPriceAboveValue;
    private String guestPriceAboveUnit;
    private Boolean guestPriceBelowEnabled = false;
    private Integer guestPriceBelowValue;
    private String guestPriceBelowUnit;
    private List<String> photoUrls;
}
