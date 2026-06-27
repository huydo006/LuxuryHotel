package com.luxuryhotel.luxury_hotel_be.controller;

import com.luxuryhotel.luxury_hotel_be.dto.PromotionDto;
import com.luxuryhotel.luxury_hotel_be.dto.PromotionRequest;
import com.luxuryhotel.luxury_hotel_be.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/promotions")
@CrossOrigin(origins = "*")
public class AdminPromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public ResponseEntity<List<PromotionDto>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPromotion(@RequestBody PromotionRequest request) {
        return ResponseEntity.ok(promotionService.createPromotion(request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> toggleStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(promotionService.togglePromotionStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePromotion(@PathVariable Integer id) {
        return ResponseEntity.ok(promotionService.deletePromotion(id));
    }
}