package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;

@Data
public class RoomDto {
    private Integer roomId;
    private String roomType;
    private Integer capacity;
    private Double defaultPrice;
    private Integer availableQuantity; // Số lượng phòng trống sau khi trừ Overlap
}