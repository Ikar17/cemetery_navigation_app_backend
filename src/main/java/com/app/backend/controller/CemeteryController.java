package com.app.backend.controller;

import com.app.backend.model.Cemetery;
import com.app.backend.repository.CemeteryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cementery")
@CrossOrigin("*")
public class CemeteryController {
    @Autowired
    private CemeteryRepository cemeteryRepository;

    @PostMapping
    public ResponseEntity<String> addNewCementery(@RequestBody Cemetery cemetery){
        if(cemetery.getName() == null || cemetery.getAddress() == null){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        cemeteryRepository.save(cemetery);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}