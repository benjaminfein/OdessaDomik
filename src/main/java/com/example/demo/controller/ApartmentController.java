package com.example.demo.controller;

import com.example.demo.dto.ApartmentDTO;
import com.example.demo.exception.ApartmentNotFoundException;
import com.example.demo.model.Apartment;
import com.example.demo.service.ApartmentService;
import jakarta.annotation.PostConstruct;
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

//    @GetMapping("api/apartment")
//    public @ResponseBody List<Apartment> getAllApartments(String name) {
//        log.info("Received GET request to fetch all apartments with name filter: {}", name);
//        return apartmentService.listOfApartments(name);
//    }
//
//    @GetMapping("api/apartment/{id}")
//    public Apartment apartmentInfo(@PathVariable Long id) {
//        try {
//            return apartmentService.getApartmentById(id);
//        } catch (Exception wrongIdWasWritten) {
//            throw new ApartmentNotFoundException("No such apartment by written id");
//        }
//    }
//
//    @PostMapping("api/apartment/create")
//    public HttpStatus createApartment(@RequestBody Apartment apartment) {
//        log.info("Received POST request to create an apartment: {}", apartment);
//        apartmentService.saveApartment(apartment);
//        return HttpStatus.OK;
//    }
//
//    @DeleteMapping("api/apartment/delete/{id}")
//    public HttpStatus deleteApartment(@PathVariable Long id) {
//        try {
//            apartmentService.deleteApartment(id);
//            return HttpStatus.NO_CONTENT;
//        } catch (Exception wrongIdWasWritten) {
//            throw new ApartmentNotFoundException("No such apartment by written id");
//        }
//    }
}
