package com.app.backend.controller;

import com.app.backend.dto.LoginDTO;
import com.app.backend.model.User;
import com.app.backend.repository.UserRepository;
import com.app.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    public ResponseEntity<String> signIn(@RequestBody LoginDTO loginDto) {
        try {
            System.out.println("Auth server started correctly for : " + loginDto.getEmail());
            Authentication authUser = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

            String jwtToken = jwtService.generateToken(authUser);
            System.out.println("Operation finished with success");
            return ResponseEntity.ok(jwtToken);
        } catch (BadCredentialsException e) {
            System.out.println("Incorrect email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect email or password");
        } catch (Exception e) {
            System.out.println("Authentication failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication failed");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody User user) {
        System.out.println("Started correctly registration for user: ");
        if (user == null || user.getEmail() == null || user.getPassword() == null) {
            return new ResponseEntity<>("Incorrect data. Email or password is null.", HttpStatus.CONFLICT);
        }

        String email = user.getEmail();
        if (userRepository.findByEmail(email).isPresent()) {
            return new ResponseEntity<>("User with this email already exists", HttpStatus.CONFLICT);
        }

        try {
            String encodedPassword = passwordEncoder.encode(user.getPassword().trim());
            user.setPassword(encodedPassword);
            userRepository.save(user);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during registration. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }

}
