package com.app.backend.controller;

import com.app.backend.dto.LoginDto;
import com.app.backend.model.User;
import com.app.backend.repository.UserRepository;
import com.app.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/signin")
    public ResponseEntity<String> signIn(@RequestBody LoginDto loginDto){
        Authentication authUser = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(),
                loginDto.getPassword()
        ));

        String jwtToken = jwtService.generateToken(authUser);
        return new ResponseEntity<>(jwtToken, HttpStatus.OK);
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

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }

}
