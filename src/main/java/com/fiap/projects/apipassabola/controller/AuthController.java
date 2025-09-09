package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.auth.*;
import com.fiap.projects.apipassabola.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register/player")
    public ResponseEntity<AuthResponse> registerPlayer(@Valid @RequestBody PlayerRegistrationRequest request) {
        AuthResponse response = authService.registerPlayer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/register/organization")
    public ResponseEntity<AuthResponse> registerOrganization(@Valid @RequestBody OrganizationRegistrationRequest request) {
        AuthResponse response = authService.registerOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/register/spectator")
    public ResponseEntity<AuthResponse> registerSpectator(@Valid @RequestBody SpectatorRegistrationRequest request) {
        AuthResponse response = authService.registerSpectator(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
