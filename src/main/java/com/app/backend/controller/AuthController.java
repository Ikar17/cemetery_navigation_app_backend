package com.app.backend.controller;

import com.app.backend.dto.LoginDto;
import com.app.backend.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {
    @PostMapping("/signin")
    public ResponseEntity<String> signIn(@RequestBody LoginDto loginDto){
        return new ResponseEntity<>("Test", HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody User user){
        return new ResponseEntity<>("Test", HttpStatus.OK);
    }

}
