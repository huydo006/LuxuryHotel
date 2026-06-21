package com.luxuryhotel.luxury_hotel_be.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "booking_details")
@Data
public class BookingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookingDetailID")
    private Integer bookingDetailId;

    // Khóa ngoại liên kết ngược lại bảng Bookings
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookingID", nullable = false)
    private Booking booking;

    // Khóa ngoại liên kết tới bảng Rooms
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomID", nullable = false)
    private Room room;

    @Column(name = "unitPrice", nullable = false)
    private Double unitPrice;
}