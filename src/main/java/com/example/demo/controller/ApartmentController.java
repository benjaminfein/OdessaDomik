package com.example.demo.controller;

import com.example.demo.dto.ApartmentDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ApartmentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@Slf4j
@RestController
@RequestMapping("/api/apartments")
@AllArgsConstructor
public class ApartmentController {
    private ApartmentService apartmentService;

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
}
