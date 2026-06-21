package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BookingRequest {
    private Integer userId;
    private Integer hotelId;
    private Integer roomId;
    private Integer promotionId; // Có thể null nếu khách không dùng mã
    private Double originalPrice;
    private Double totalPaid;
    private Double depositAmount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}