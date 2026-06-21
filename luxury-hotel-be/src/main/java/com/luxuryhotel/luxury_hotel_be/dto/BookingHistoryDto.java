package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BookingHistoryDto {
    private Integer bookingID;
    private Integer hotelID;
    private String nameHotel;
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Double totalPrice;
    private String status;
}