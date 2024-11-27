package com.riaydev.bankingapp.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riaydev.bankingapp.DTO.AuthRequest;
import com.riaydev.bankingapp.DTO.TokenResponse;
import com.riaydev.bankingapp.DTO.UserRequest;
import com.riaydev.bankingapp.Security.JwtTokenProvider;
import com.riaydev.bankingapp.Services.TokenBlacklistService;
import com.riaydev.bankingapp.Services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequest request) throws Exception {

        final UserRequest response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody AuthRequest loginRequest) {
        final TokenResponse jwt = userService.loginUser(loginRequest);
        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String token = jwtTokenProvider.extractTokenFromHeader(request);

        if (token != null && !token.isEmpty()) {
            long expirationTime = jwtTokenProvider.extractExpiration(token).getTime();
            tokenBlacklistService.addToBlacklist(token, expirationTime);
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(
                    Map.of("message", "Logout successful"));
        }
        return ResponseEntity.badRequest().body("Token not provided");
    }

}
