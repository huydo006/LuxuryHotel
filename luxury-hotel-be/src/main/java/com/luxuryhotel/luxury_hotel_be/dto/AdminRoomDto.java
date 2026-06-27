package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;

@Data
public class AdminRoomDto {
    private Integer roomID;
    private String roomType;
    private Integer capacity;
    private Double defaultPrice;
    private Integer quantity;
}