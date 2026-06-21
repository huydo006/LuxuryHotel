package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private boolean success;
    private String message;
    private UserDto user; // Có thể null nếu đăng nhập thất bại
    
    public AuthResponse(boolean success, String message, UserDto user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }
}