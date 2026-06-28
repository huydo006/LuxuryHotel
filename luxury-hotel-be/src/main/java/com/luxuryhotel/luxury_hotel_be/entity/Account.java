package com.luxuryhotel.luxury_hotel_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "accounts") // Ánh xạ đúng tên bảng trong MySQL
@Data
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accountID") // Ánh xạ với cột accountID trong DB
    private Integer accountId;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "passwords", nullable = false, length = 255)
    private String password;

    @Column(name = "fullName", length = 100)
    private String fullName;

    @Column(name ="email" ,length = 100)
    private String email;

    @Column(name = "phoneNumber", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING) // Lưu giá trị ENUM dưới dạng chuỗi (Manager/Customer)
    @Column(nullable = false)
    private Role role;

    // Định nghĩa Enum cho Role
    public enum Role {
        Manager, Customer
    }
}