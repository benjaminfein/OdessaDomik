package com.example.demo.controller;

import com.example.demo.dto.DateRangeRuleDTO;
import com.example.demo.service.DateRangeRuleService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/apartments/{id}/rules")
@AllArgsConstructor
public class DateRangeRuleController {
    private DateRangeRuleService dateRangeRuleService;

    @GetMapping
    public ResponseEntity<List<DateRangeRuleDTO>> getRules(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(dateRangeRuleService.getRules(id, from, to));
    }

    @PostMapping
    public ResponseEntity<DateRangeRuleDTO> saveRule(
            @PathVariable Long id,
            @RequestBody DateRangeRuleDTO dto
    ) {
        return new ResponseEntity<>(dateRangeRuleService.saveRule(id, dto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long ruleId) {
        dateRangeRuleService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }
}
