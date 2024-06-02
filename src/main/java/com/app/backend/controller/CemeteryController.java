package com.app.backend.controller;

import com.app.backend.model.Cemetery;
import com.app.backend.repository.CemeteryRepository;
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

    @PostMapping
    public ResponseEntity<String> addNewCemetery(@RequestBody Cemetery cemetery) {
        if (cemetery.getName() == null || cemetery.getAddress() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        cemeteryRepository.save(cemetery);
        return new ResponseEntity<>(HttpStatus.CREATED);
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
}
