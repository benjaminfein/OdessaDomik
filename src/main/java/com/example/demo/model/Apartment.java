package com.example.demo.model;

import com.example.demo.dto.ApartmentDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name = "apartment_info", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
public class Apartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "shortDescription")
    private String shortDescription;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "address")
    private String address;
    @Column(name = "price")
    private Integer price;
    @Column(name = "hasParkingLot")
    private Boolean hasParkingLot = false;
    @Column(name = "hasWiFi")
    private Boolean hasWiFi = false;
    @Column(name = "floorNumber")
    private Integer floorNumber;
    @Column(name = "areaOfApartment")
    private Integer areaOfApartment;
    @Column(name = "hasSeaView")
    private Boolean hasSeaView = false;
    @Column(name = "countOfSleepPlaces")
    private Integer countOfSleepPlaces;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "apartment_photos", joinColumns = @JoinColumn(name = "apartment_id"))
    @Column(name = "photo_url")
    private List<String> photoUrls;

    public ApartmentDTO getApartmentDTO() {
        ApartmentDTO apartmentDTO = new ApartmentDTO();

        apartmentDTO.setId(id);
        apartmentDTO.setName(name);
        apartmentDTO.setShortDescription(shortDescription);
        apartmentDTO.setDescription(description);
        apartmentDTO.setAddress(address);
        apartmentDTO.setPrice(price);
        apartmentDTO.setHasParkingLot(hasParkingLot);
        apartmentDTO.setHasWiFi(hasWiFi);
        apartmentDTO.setFloorNumber(floorNumber);
        apartmentDTO.setAreaOfApartment(areaOfApartment);
        apartmentDTO.setHasSeaView(hasSeaView);
        apartmentDTO.setCountOfSleepPlaces(countOfSleepPlaces);

        return apartmentDTO;
    }

    public String getComplexName() {
        String key = getAddressKey();
        return switch (key) {
            case "Гагаринское плато 5/2" -> "Гагарин Плаза";
            case "Улица Генуэзская 3в" -> "42 Жемчужина";
            case "Улица Генуэзская 3" -> "26 Жемчужина";
            default -> "Неизвестный комплекс";
        };
    }

    // Метод для извлечения ключа из адреса
    private String getAddressKey() {
        if (address.toLowerCase().contains("гагаринское плато")) {
            return "Гагаринское плато 5/2";
        } else if (address.toLowerCase().contains("улица генуэзская 3в")) {
            return "Улица Генуэзская 3в";
        } else if (address.toLowerCase().contains("улица генуэзская 3")) {
            return "Улица Генуэзская 3";
        } else {
            return "неизвестный адрес";
        }
    }
}
