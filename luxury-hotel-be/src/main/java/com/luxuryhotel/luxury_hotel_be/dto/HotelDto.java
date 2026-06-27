package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;
import java.util.List;
@Data
public class HotelDto {
    private Integer id;           // Khớp với hotel.id trong JS
    private String name;          // Khớp với hotel.name
    private String location;      // Khớp với hotel.location
    private Integer rating;       // Khớp với hotel.rating
    private String description;   // Khớp với hotel.description
    private String image;         // Khớp với hotel.image
    private Integer bookingsCount;// Khớp với hotel.bookingsCount
    private Integer maxCapacity;
    private List<String> amenities;
}