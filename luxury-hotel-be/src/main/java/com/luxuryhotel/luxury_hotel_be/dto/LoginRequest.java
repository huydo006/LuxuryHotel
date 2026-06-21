package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}