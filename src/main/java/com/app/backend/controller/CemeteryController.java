package com.app.backend.controller;

import com.app.backend.model.Cementery;
import com.app.backend.repository.CementeryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cementery")
@CrossOrigin("*")
public class CementeryController {
    @Autowired
    private CementeryRepository cementeryRepository;

    @PostMapping
    public ResponseEntity<String> addNewCementery(@RequestBody Cementery cementery){
        if(cementery.getName() == null || cementery.getAddress() == null){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        cementeryRepository.save(cementery);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}