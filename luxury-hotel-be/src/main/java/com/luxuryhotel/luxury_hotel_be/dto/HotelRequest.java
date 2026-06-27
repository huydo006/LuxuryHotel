package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;
import java.util.List;
@Data
public class HotelRequest {
    private String name;
    private String location; // Tương ứng với cột 'address' trong Database
    private String image;
    private String description;
    private List<String> amenities;
}