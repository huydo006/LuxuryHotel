package com.luxuryhotel.luxury_hotel_be.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.luxuryhotel.luxury_hotel_be.dto.ReviewDto;
import com.luxuryhotel.luxury_hotel_be.dto.ReviewRequest;
import com.luxuryhotel.luxury_hotel_be.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> createReview(
            @RequestPart("review") ReviewRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        return ResponseEntity.ok(reviewService.createReview(request, images));
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<ReviewDto>> getReviews(@PathVariable Integer hotelId) {
        return ResponseEntity.ok(reviewService.getReviewsByHotel(hotelId));
    }
}
