package com.luxuryhotel.luxury_hotel_be.dto;

import lombok.Data;

@Data
public class CustomerRequest {
    private String fullName;
    private String email;
    private String username;
    private String password;
}