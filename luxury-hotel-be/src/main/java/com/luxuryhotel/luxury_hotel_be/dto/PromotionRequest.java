package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PromotionRequest {
    private String discountCode;
    private String namePromo;
    private Double discountPercent;
    private Double maxDiscountAmount;
    private Double minBookingValue;
    private Integer usageLimit;
    private LocalDate startDate;
    private LocalDate endDate;
}