package com.example.demo.controller;

import com.example.demo.dto.ApartmentDTO;
import com.example.demo.dto.ReservationDTO;
import com.example.demo.mapper.ApartmentMapper;
import com.example.demo.service.ApartmentService;
import com.example.demo.service.ReservationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.List;

@CrossOrigin("*")
@Slf4j
@RestController
@RequestMapping("/api/apartments")
@AllArgsConstructor
public class ApartmentController {
    private ApartmentService apartmentService;
    private ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApartmentDTO> createApartment(@RequestBody ApartmentDTO apartmentDTO) {
        ApartmentDTO savedApartment = apartmentService.createApartment(apartmentDTO);
        return new ResponseEntity<>(savedApartment, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<ApartmentDTO> getApartmentById(@PathVariable Long id) {
        ApartmentDTO apartmentDTO = apartmentService.getApartmentById(id);
        return ResponseEntity.ok(apartmentDTO);
    }

    @GetMapping
    public ResponseEntity<List<ApartmentDTO>> getAllApartments() {
        List<ApartmentDTO> apartments = apartmentService.getAllApartments();
        return ResponseEntity.ok(apartments);
    }

    @PutMapping("{id}")
    public ResponseEntity<ApartmentDTO> updateApartment(@PathVariable("id") Long id,
                                                        @RequestBody ApartmentDTO apartmentToUpdate) {
        log.info(apartmentToUpdate.getAddress());
        ApartmentDTO apartmentDTO = apartmentService.updateApartment(id, apartmentToUpdate);
        return ResponseEntity.ok(apartmentDTO);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteApartment(@PathVariable("id") Long id) {
        apartmentService.deleteApartment(id);
        return ResponseEntity.ok("Apartment deleted successfully!");
    }

    //Working with reservation

    @GetMapping("/available")
    public ResponseEntity<List<ApartmentDTO>> getAvailableApartments(
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut,
            @RequestParam(required = false, defaultValue = "2") int guestCount) {

        List<ApartmentDTO> availableApartments;

        if (checkIn != null && checkOut != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = (checkIn == null || checkIn.isEmpty()) ? null : LocalDate.parse(checkIn, formatter);
            LocalDate endDate = (checkOut == null || checkOut.isEmpty()) ? null : LocalDate.parse(checkOut, formatter);
            availableApartments = apartmentService.findAvailableApartments(startDate, endDate, guestCount);
        } else {
            availableApartments = apartmentService.findApartmentsByGuestCount(guestCount);
        }

        return ResponseEntity.ok(availableApartments);
    }

    //Endpoints for reservations(rn its for test)
    @PostMapping("/create-reservation")
    public ResponseEntity<ReservationDTO> createReservation(@RequestBody ReservationDTO reservationDTO) {
        ReservationDTO savedReservation = reservationService.createReservation(reservationDTO);
        return new ResponseEntity<>(savedReservation, HttpStatus.CREATED);
    }

    @GetMapping("/get-reservation")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        List<ReservationDTO> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }
}
