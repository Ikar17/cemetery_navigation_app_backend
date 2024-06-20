package com.app.backend.controller;

import com.app.backend.dto.DecedentDTO;
import com.app.backend.model.Cemetery;
import com.app.backend.model.Decedent;
import com.app.backend.model.User;
import com.app.backend.repository.CemeteryRepository;
import com.app.backend.repository.DecedentRepository;
import com.app.backend.repository.UserRepository;
import com.app.backend.service.DecedentService;
import com.app.backend.service.ModerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/decedent")
@CrossOrigin("*")
public class DecedentController {

    private static final Logger logger = LoggerFactory.getLogger(DecedentController.class);

    @Autowired
    private CemeteryRepository cemeteryRepository;

    @Autowired
    private DecedentRepository decedentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DecedentService decedentService;
    @Autowired
    private ModerationService moderationService;


    @PostMapping("/add")
    public ResponseEntity<String> addDecedent(@RequestParam("decedent") String decedentJson, @RequestParam(value = "tombstoneImage", required = false) MultipartFile tombstoneImage) {
        logger.info("Received request to add decedent");
        logger.info("Decedent JSON: {}", decedentJson);

        DecedentDTO decedentDTO;
        try {
            decedentDTO = objectMapper.readValue(decedentJson, DecedentDTO.class);
            logger.info("Parsed decedent DTO: {}", decedentDTO);
        } catch (Exception e) {
            logger.error("Error parsing decedent JSON: {}", e.getMessage(), e);
            return new ResponseEntity<>("Invalid decedent data", HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<String> validationResponse = validateDecedent(decedentDTO);
        if (validationResponse != null) {
            logger.warn("Validation failed: {}", validationResponse.getBody());
            return validationResponse;
        }

        try {
            boolean isNameAllowed = moderationService.checkTextContent(decedentDTO.getName());
            boolean isDescriptionAllowed = moderationService.checkTextContent(decedentDTO.getDescription());

            if (!isNameAllowed || !isDescriptionAllowed) {
                return new ResponseEntity<>("Text contains offensive content.", HttpStatus.BAD_REQUEST);
            }

            if (tombstoneImage != null && !tombstoneImage.isEmpty()) {
                boolean isImageSafe = moderationService.checkImageContent(tombstoneImage);
                if (!isImageSafe) {
                    return new ResponseEntity<>("The uploaded image is not safe.", HttpStatus.BAD_REQUEST);
                }
            }
            Decedent decedent = new Decedent();
            decedent.setName(decedentDTO.getName());
            decedent.setSurname(decedentDTO.getSurname());

            if(decedentDTO.getBirthDate() != null){
                decedent.setBirthDate(decedentDTO.getBirthDate());
            }
            if(decedentDTO.getDeathDate() != null){
                decedent.setDeathDate(decedentDTO.getDeathDate());
            }
            if(decedentDTO.getDescription() != null){
                decedent.setDescription(decedentDTO.getDescription());
            }

            decedent.setLatitude(Float.valueOf(0));
            decedent.setLongitude(Float.valueOf(0));

            Optional<Cemetery> cemeteryOptional = cemeteryRepository.findById(decedentDTO.getCemeteryId());
            if (cemeteryOptional.isPresent()) {
                decedent.setCemetery(cemeteryOptional.get());
            } else {
                return new ResponseEntity<>("Invalid cemetery ID", HttpStatus.BAD_REQUEST);
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> optionalUser = userRepository.findByEmail(authentication.getName());
            optionalUser.ifPresent(decedent::setUser);

            if (tombstoneImage != null && !tombstoneImage.isEmpty()) {
                decedent.setTombstoneImage(tombstoneImage.getBytes());
                logger.info("Tombstone image uploaded: {}", tombstoneImage.getOriginalFilename());
            } else {
                logger.info("No tombstone image uploaded");
            }

            decedentRepository.save(decedent);
            logger.info("Decedent saved successfully");
            return new ResponseEntity<>("Decedent added successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving decedent: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error saving decedent: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<String> validateDecedent(DecedentDTO decedentDTO) {
        if (decedentDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Decedent data is null");
        }
        if (decedentDTO.getBirthDate() != null && decedentDTO.getBirthDate().isAfter(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Birth date cannot be in the future");
        }
        if (decedentDTO.getDeathDate() != null) {
            if (decedentDTO.getBirthDate() != null && decedentDTO.getBirthDate().isAfter(decedentDTO.getDeathDate())) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Death date cannot be after birth date");
            }
        }
        return null;
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
            logger.error("Error fetching decedent by ID: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Decedent> updateDecedentById(@PathVariable Integer id, @RequestBody Decedent decedent) {
        try {
            Optional<Decedent> decedentOptional = decedentRepository.findById(id);
            if (decedentOptional.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            Decedent originalDecedent = decedentOptional.get();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User decedentOwner = originalDecedent.getUser();
            if (decedentOwner == null || !decedentOwner.getEmail().equals(authentication.getName())) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            if (decedent.getBirthDate() != null) originalDecedent.setBirthDate(decedent.getBirthDate());
            if (decedent.getDeathDate() != null) originalDecedent.setDeathDate(decedent.getDeathDate());
            if (decedent.getName() != null) originalDecedent.setName(decedent.getName());
            if (decedent.getSurname() != null) originalDecedent.setSurname(decedent.getSurname());
            if (decedent.getDescription() != null) originalDecedent.setDescription(decedent.getDescription());
            if (decedent.getLatitude() != null) originalDecedent.setLatitude(decedent.getLatitude());
            if (decedent.getLongitude() != null) originalDecedent.setLongitude(decedent.getLongitude());
            if (decedent.getTombstoneImage() != null) originalDecedent.setTombstoneImage(decedent.getTombstoneImage());

            decedentRepository.save(originalDecedent);
            return new ResponseEntity<>(originalDecedent, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating decedent: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<DecedentDTO>> searchDecedents(
            @RequestParam String name,
            @RequestParam String surname,
            @RequestParam(required = false) String city) {
        try {
            System.out.println("Started search operation");
            name = name != null ? name : "";
            surname = surname != null ? surname : "";
            city = city != null ? city : "";

            if (name.length() < 2 && surname.length() < 2) {
                return ResponseEntity.notFound().build();
            }

            List<DecedentDTO> decedents = decedentService.findByNameAndSurnameAndCity(name, surname, city);

            if (decedents.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(decedents);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

}
