package com.luxuryhotel.luxury_hotel_be.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookingID")
    private Integer bookingId;

    // Khóa ngoại liên kết tới bảng Accounts
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountID", nullable = false)
    private Account account;

    // Khóa ngoại liên kết tới bảng Promotions (Có thể null nếu khách không dùng mã)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotionID")
    private Promotion promotion;

    @Column(name = "totalPrice", nullable = false)
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.processing;

    @Column(name = "checkInDate", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "checkOutDate", nullable = false)
    private LocalDate checkOutDate;

    @CreationTimestamp // Tự động lấy giờ hiện tại của hệ thống khi insert
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "originalPrice")
    private Double originalPrice;

    @Column(name = "depositAmount")
    private Double depositAmount;

    // Enum định nghĩa trạng thái đơn hàng
    public enum Status {
        processing, success, cancelled
    }
}