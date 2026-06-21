package com.luxuryhotel.luxury_hotel_be.dto;
import lombok.Data;

@Data
public class RoomSimpleDto {
    private Integer id;
    private String name;     // Tương ứng với roomType trong DB
    private Integer capacity;
    private Double price;
    private Integer availableQuantity;
}