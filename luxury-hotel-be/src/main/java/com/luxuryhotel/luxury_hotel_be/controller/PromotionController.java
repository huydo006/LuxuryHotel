package com.luxuryhotel.luxury_hotel_be.controller;

import com.luxuryhotel.luxury_hotel_be.dto.PromoApplyRequest;
import com.luxuryhotel.luxury_hotel_be.dto.PromotionDto;
import com.luxuryhotel.luxury_hotel_be.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "*")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping("/available")
    public ResponseEntity<List<PromotionDto>> getAvailablePromotions() {
        return ResponseEntity.ok(promotionService.getAvailablePromotions());
    }

    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyPromotion(@RequestBody PromoApplyRequest request) {
        return ResponseEntity.ok(promotionService.applyPromotion(request));
    }
}