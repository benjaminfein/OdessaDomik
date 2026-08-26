package com.example.demo.mapper;

import com.example.demo.dto.ApartmentDTO;
import com.example.demo.enums.PriceUnit;
import com.example.demo.model.Apartment;

public class ApartmentMapper {
    public static ApartmentDTO mapToApartmentDTO(Apartment apartment) {
        return new ApartmentDTO(
                apartment.getId(),
                apartment.getName(),
                apartment.getShortDescription(),
                apartment.getDescription(),
                apartment.getAddress(),
                apartment.getPrice(),
                apartment.getHasParkingLot(),
                apartment.getHasWiFi(),
                apartment.getFloorNumber(),
                apartment.getAreaOfApartment(),
                apartment.getHasSeaView(),
                apartment.getCountOfSleepPlaces(),
                apartment.getGapDays() != null ? apartment.getGapDays() : 0,
                apartment.getGuestPriceAboveEnabled(),
                apartment.getGuestPriceAboveValue(),
                apartment.getGuestPriceAboveUnit() != null ? apartment.getGuestPriceAboveUnit().name() : null,
                apartment.getGuestPriceBelowEnabled(),
                apartment.getGuestPriceBelowValue(),
                apartment.getGuestPriceBelowUnit() != null ? apartment.getGuestPriceBelowUnit().name() : null,
                apartment.getPhotoUrls()
        );
    }

    public static Apartment mapToApartment(ApartmentDTO apartmentDTO) {
        return new Apartment(
                apartmentDTO.getId(),
                apartmentDTO.getName(),
                apartmentDTO.getShortDescription(),
                apartmentDTO.getDescription(),
                apartmentDTO.getAddress(),
                apartmentDTO.getPrice(),
                apartmentDTO.getHasParkingLot(),
                apartmentDTO.getHasWiFi(),
                apartmentDTO.getFloorNumber(),
                apartmentDTO.getAreaOfApartment(),
                apartmentDTO.getHasSeaView(),
                apartmentDTO.getCountOfSleepPlaces(),
                apartmentDTO.getGapDays() != null ? apartmentDTO.getGapDays() : 0,
                apartmentDTO.getGuestPriceAboveEnabled(),
                apartmentDTO.getGuestPriceAboveValue(),
                apartmentDTO.getGuestPriceAboveUnit() != null ? PriceUnit.valueOf(apartmentDTO.getGuestPriceAboveUnit()) : null,
                apartmentDTO.getGuestPriceBelowEnabled(),
                apartmentDTO.getGuestPriceBelowValue(),
                apartmentDTO.getGuestPriceBelowUnit() != null ? PriceUnit.valueOf(apartmentDTO.getGuestPriceBelowUnit()) : null,
                apartmentDTO.getPhotoUrls()
        );
    }
}

