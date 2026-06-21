package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;

@Data
public class UserDto {
    private Integer accountId;
    private String username;
    private String fullName;
    private String email;
    private String role; // Sẽ map "Manager" thành "admin" để khớp với JS
}