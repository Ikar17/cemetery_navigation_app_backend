package com.app.backend.controller;

import com.app.backend.dto.LoginDto;
import com.app.backend.model.User;
import com.app.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @PostMapping("/signin")
    public ResponseEntity<String> signIn(@RequestBody LoginDto loginDto){
        return new ResponseEntity<>("Test", HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody User user){
        if(user == null || user.getEmail() == null || user.getPassword() == null){
            return new ResponseEntity<>("Incorrect data. Email or password is null.", HttpStatus.FORBIDDEN);
        }

        String email = user.getEmail();
        if(userRepository.findByEmail(email).isPresent()){
            return new ResponseEntity<>("User with this email already exists", HttpStatus.FORBIDDEN);
        }

        userRepository.save(user);
        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }

}
