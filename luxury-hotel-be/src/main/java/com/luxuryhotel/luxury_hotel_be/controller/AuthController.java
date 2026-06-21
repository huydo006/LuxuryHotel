package com.luxuryhotel.luxury_hotel_be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.luxuryhotel.luxury_hotel_be.dto.AuthResponse;
import com.luxuryhotel.luxury_hotel_be.dto.LoginRequest;
import com.luxuryhotel.luxury_hotel_be.dto.RegisterRequest;
import com.luxuryhotel.luxury_hotel_be.service.AuthService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Quan trọng: Cho phép JS gọi không bị lỗi CORS
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
}