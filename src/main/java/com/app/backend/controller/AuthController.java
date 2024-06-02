package com.app.backend.controller;

import com.app.backend.dto.LoginDTO;
import com.app.backend.model.User;
import com.app.backend.repository.UserRepository;
import com.app.backend.service.EmailService;
import com.app.backend.service.JwtService;
import com.app.backend.utils.AccountType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    @Autowired
    private EmailService emailService;

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
            user.setAccountType(AccountType.USER);
            userRepository.save(user);

            String subject = "Registration Confirmation";
            String text = "Dear " + user.getUsername() + ",\n\nThank you for registering. Your registration was successful.\n\nBest Regards,\nSpokoj Ducha";
            emailService.sendRegistrationEmail(user.getEmail(), subject, text);

        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during registration. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }

    @GetMapping("/user-id")
    public ResponseEntity<Integer> getUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            Optional<User> userOptional = userRepository.findByEmail(userEmail);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok(userOptional.get().getId());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
