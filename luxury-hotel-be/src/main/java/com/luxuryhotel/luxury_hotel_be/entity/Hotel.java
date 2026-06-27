package com.luxuryhotel.luxury_hotel_be.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "hotels")
@Data
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hotelID")
    private Integer hotelId;

    @Column(name = "nameHotel", nullable = false, length = 255)
    private String nameHotel;

    @Column(name = "address", columnDefinition = "TEXT", nullable = false)
    private String address;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "bookingsCount")
    private Integer bookingsCount;

    @Column(name = "amenities")
    private String amenities;
}