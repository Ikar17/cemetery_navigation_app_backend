package com.app.backend.controller;

import com.app.backend.model.Decedent;
import com.app.backend.repository.DecedentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/decedent")
@CrossOrigin("*")
public class DecedentController {

    @Autowired
    private DecedentRepository decedentRepository;

    @PostMapping("/add")
    public ResponseEntity<String> addDecedent(@RequestBody Decedent decedent) {
        ResponseEntity<String> validationResponse = validateDecedent(decedent);
        if (validationResponse != null) {
            return validationResponse;
        }

        try {
            decedentRepository.save(decedent);
            return new ResponseEntity<>("Decedent added successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error saving decedent: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<String> validateDecedent(Decedent decedent){
        if (decedent == null) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Decedent data is null");
        }
        if (decedent.getBirthDate() != null && decedent.getBirthDate().isAfter(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Birth date cannot be in the future");
        }
        if (decedent.getDeathDate() != null) {
            if (decedent.getDeathDate().isAfter(LocalDate.now())) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Death date cannot be in the future");
            }
            if (decedent.getBirthDate() != null && decedent.getBirthDate().isAfter(decedent.getDeathDate())) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Death date cannot be after birth date");
            }
        }
        return null;
    }
}
