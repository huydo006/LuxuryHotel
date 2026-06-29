package com.luxuryhotel.luxury_hotel_be.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
@Data
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotionID")
    private Integer promotionId;

    @Column(name = "discountCode", nullable = false, unique = true, length = 50)
    private String discountCode;

    @Column(name = "namePromo", nullable = false, length = 255)
    private String namePromo;

    @Column(name = "discountPercent", nullable = false)
    private Double discountPercent;

    @Column(name = "maxDiscountAmount", nullable = false)
    private Double maxDiscountAmount;

    @Column(name = "minBookingValue", nullable = false)
    private Double minBookingValue;

    @Column(name = "usageLimit", nullable = false)
    private Integer usageLimit;

    @Column(name = "usedCount")
    private Integer usedCount = 0;

    @Column(name = "startDate", nullable = false)
    private LocalDate startDate;

    @ManyToOne
    @JoinColumn(name = "createdBy_AccountID")
    private Account createdBy;

    @Column(name = "endDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "isValid")
    private Integer isValid = 1;
}