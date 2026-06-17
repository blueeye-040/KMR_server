package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.AuthResponse;
import com.kmr.marketplace.dto.LoginRequest;
import com.kmr.marketplace.dto.RegisterRequest;
import com.kmr.marketplace.entity.User;
import com.kmr.marketplace.security.JwtService;
import com.kmr.marketplace.security.TokenBlacklistService;
import com.kmr.marketplace.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;

    public AuthController(AuthService authService,
                          JwtService jwtService,
                          TokenBlacklistService blacklistService) {
        this.authService      = authService;
        this.jwtService       = jwtService;
        this.blacklistService = blacklistService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Instant expiry = jwtService.extractExpiry(token);
            blacklistService.blacklist(token, expiry);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of(
                "id",    user.getId().toString(),
                "name",  user.getName(),
                "email", user.getEmail(),
                "role",  user.getRole().name()
        ));
    }
}
