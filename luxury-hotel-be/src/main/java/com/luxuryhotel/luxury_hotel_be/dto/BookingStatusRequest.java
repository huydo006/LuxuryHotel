package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;

@Data
public class BookingStatusRequest {
    private String status;  
    private Integer adminId; 
}