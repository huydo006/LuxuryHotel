package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private Integer userId;
    private Integer hotelId;
    private Integer rating;
    private String comment;
}
