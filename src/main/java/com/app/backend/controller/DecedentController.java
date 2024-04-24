package com.app.backend.controller;

import com.app.backend.model.Decedent;
import com.app.backend.model.User;
import com.app.backend.repository.DecedentRepository;
import com.app.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/decedent")
@CrossOrigin("*")
public class DecedentController {

    @Autowired
    private DecedentRepository decedentRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<String> addDecedent(@RequestBody Decedent decedent) {
        ResponseEntity<String> validationResponse = validateDecedent(decedent);
        if (validationResponse != null) {
            return validationResponse;
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> optionalUser = userRepository.findByEmail(authentication.getName());
            optionalUser.ifPresent(decedent::setUser);
            if(decedent.getLongitude() == null) decedent.setLongitude((float)0);
            if(decedent.getLatitude() == null) decedent.setLatitude((float)0);
            decedentRepository.save(decedent);
            return new ResponseEntity<>("Decedent added successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error saving decedent: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Decedent> getDecedentById(@PathVariable Integer id) {
        try {
            Optional<Decedent> decedentOptional = decedentRepository.findById(id);
            if (decedentOptional.isPresent()) {
                return new ResponseEntity<>(decedentOptional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Decedent> updateDecedentById(@PathVariable Integer id,
                                                       @RequestBody Decedent decedent) {
        try{
            Optional<Decedent> decedentOptional = decedentRepository.findById(id);
            if (decedentOptional.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            Decedent originalDecedent = decedentOptional.get();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User decedentOwner = originalDecedent.getUser();
            if(decedentOwner == null || !decedentOwner.getEmail().equals(authentication.getName())){
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            if(decedent.getBirthDate() != null) originalDecedent.setBirthDate(decedent.getBirthDate());
            if(decedent.getDeathDate() != null) originalDecedent.setDeathDate(decedent.getDeathDate());
            if(decedent.getName() != null) originalDecedent.setName(decedent.getName());
            if(decedent.getSurname() != null) originalDecedent.setSurname(decedent.getSurname());
            if(decedent.getDescription() != null) originalDecedent.setDescription(decedent.getDescription());
            if(decedent.getLatitude() != null) originalDecedent.setLatitude(decedent.getLatitude());
            if(decedent.getLongitude() != null) originalDecedent.setLongitude(decedent.getLongitude());
            if(decedent.getTombstoneImage() != null) originalDecedent.setTombstoneImage(decedent.getTombstoneImage());

            decedentRepository.save(originalDecedent);
            return new ResponseEntity<>(originalDecedent, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
