package com.luxuryhotel.luxury_hotel_be.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "rooms")
@Data
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roomID")
    private Integer roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotelID", nullable = false)
    private Hotel hotel;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "roomType", nullable = false, length = 100)
    private String roomType;

    @Column(name = "quantity")
    private Integer quantity; // TỔNG QUỸ PHÒNG CỐ ĐỊNH

    @Column(name = "defaultPrice")
    private Double defaultPrice;
}