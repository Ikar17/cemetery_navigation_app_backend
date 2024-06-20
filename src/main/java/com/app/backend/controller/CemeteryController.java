package com.app.backend.controller;

import com.app.backend.model.Cemetery;
import com.app.backend.repository.CemeteryRepository;
import com.app.backend.service.ModerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cemetery")
@CrossOrigin("*")
public class CemeteryController {
    @Autowired
    private CemeteryRepository cemeteryRepository;
    @Autowired
    private ModerationService moderationService;

    @PostMapping
    public ResponseEntity<String> addNewCemetery(@RequestBody Cemetery cemetery) {
        try{
            if (cemetery.getName() == null || cemetery.getAddress() == null) {
                return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            }

            //trzeba uruchomić aplikacje do sprawdzania w Pythonie i odkomentować

            boolean isNameAllowed = moderationService.checkTextContent(cemetery.getName());
            boolean isAddressAllowed = moderationService.checkTextContent(cemetery.getAddress());

            if (!isNameAllowed || !isAddressAllowed) {
                return new ResponseEntity<>("Content contains offensive language.", HttpStatus.BAD_REQUEST);
            }

            //rozdzielam adres na ulice oraz miasto
            String address = cemetery.getAddress();
            int commaIndex = address.indexOf(',');
            int dotIndex = address.indexOf('.');

            int splitIndex;
            if (commaIndex != -1) {
                splitIndex = commaIndex;
            } else {
                splitIndex = dotIndex;
            }

            if(splitIndex != -1){
                String streetPart = address.substring(0, splitIndex).trim();
                String cityPart = address.substring(splitIndex + 1).trim();
                cemetery.setAddress(streetPart);
                cemetery.setCity(cityPart);
            }

            //sprawdzam czy juz taki cmentarz istnieje
            Optional<Cemetery> existingCemetery = cemeteryRepository.findByAddressContainingAndCityContaining(cemetery.getAddress(), cemetery.getCity());
            if(existingCemetery.isPresent()){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            cemeteryRepository.save(cemetery);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping
    public ResponseEntity<List<Cemetery>> getAllCemeteries() {
        List<Cemetery> cemeteries = cemeteryRepository.findAll();
        return new ResponseEntity<>(cemeteries, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cemetery> getCemeteryById(@PathVariable Integer id) {
        Optional<Cemetery> cemetery = cemeteryRepository.findById(id);
        if (cemetery.isPresent()) {
            return new ResponseEntity<>(cemetery.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Cemetery>> findByCity(@RequestParam String city){
        try{
            List<Cemetery> cemeteries = cemeteryRepository.findByCity(city);
            return new ResponseEntity<>(cemeteries, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
