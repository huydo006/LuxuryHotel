package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;

@Data
public class PromoApplyRequest {
    private Integer userId;
    private String discountCode;
    private Double bookingTotal;
}