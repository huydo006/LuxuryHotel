package com.luxuryhotel.luxury_hotel_be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CustomerDto {
    @JsonProperty("accountID")
    private Integer accountID;
    private String username;
    private String fullName;
    private String email;
}