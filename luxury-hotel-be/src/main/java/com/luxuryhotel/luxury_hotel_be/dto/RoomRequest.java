package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;

@Data
public class RoomRequest {
    private Integer hotelId;
    private String roomType;
    private Integer capacity;
    private Double price;
    private Integer quantity;
    private Integer adminId;
}