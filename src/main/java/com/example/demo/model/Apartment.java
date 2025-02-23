package com.example.demo.model;

import com.example.demo.dto.ApartmentDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "apartment_info", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
public class Apartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false, unique = true)
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

//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
//    mappedBy = "product")
//    private List<Image> images = new ArrayList<>();
//    private Long previewImageId;
//    private LocalDateTime dateOfCreated;
//
//    @PrePersist
//    private void init() {
//        dateOfCreated = LocalDateTime.now();
//    }

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
}
