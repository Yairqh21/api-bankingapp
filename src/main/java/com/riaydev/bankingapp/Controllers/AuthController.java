package com.riaydev.bankingapp.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riaydev.bankingapp.DTO.AuthRequest;
import com.riaydev.bankingapp.DTO.TokenDTO;
import com.riaydev.bankingapp.DTO.UserDTO;
import com.riaydev.bankingapp.Services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class AuthController {

    private final UserService service;

    @PostMapping("/send-otp")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO request) {
        System.out.println(request);
        try {
            final UserDTO response = service.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> loginUser(@RequestBody AuthRequest loginRequest) {
        try {
            final TokenDTO jwt = service.loginUser(loginRequest);
            return ResponseEntity.ok(jwt);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad credentials");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body( e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
    

}
