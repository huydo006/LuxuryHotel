package com.luxuryhotel.luxury_hotel_be.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewDto {
    private String username;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}