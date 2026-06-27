package com.luxuryhotel.luxury_hotel_be.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/amenities")
@CrossOrigin(origins = "*")
public class AmenityController {

    @GetMapping
    public ResponseEntity<List<String>> getAvailableAmenities() {
        // Đây là kho tiện ích có sẵn của hệ thống. 
        // Sau này nếu có database riêng cho tiện ích, bạn chỉ cần thay bằng amenityRepository.findAll()
        List<String> amenities = Arrays.asList(
            "🏊 Hồ bơi vô cực", 
            "📶 WiFi Tốc độ cao", 
            "🍽️ Buffet sáng", 
            "💆 Spa & Massage", 
            "🏋️ Phòng Gym", 
            "🅿️ Bãi đậu xe",
            "🍸 Quầy Bar",
            "🛁 Bồn tắm nước nóng",
            "⛳ Sân Golf"
        );
        return ResponseEntity.ok(amenities);
    }
}