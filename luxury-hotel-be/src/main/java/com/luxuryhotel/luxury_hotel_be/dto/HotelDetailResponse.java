package com.luxuryhotel.luxury_hotel_be.dto;
import lombok.Data;
import java.util.List;

@Data
public class HotelDetailResponse {
    private boolean success;
    private String message;
    private HotelDto hotel;
    private List<RoomSimpleDto> rooms;
    private List<ReviewDto> reviews;
}