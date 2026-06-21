package com.luxuryhotel.luxury_hotel_be.controller;

import com.luxuryhotel.luxury_hotel_be.dto.HotelDetailResponse;
import com.luxuryhotel.luxury_hotel_be.dto.HotelDto;
import com.luxuryhotel.luxury_hotel_be.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = "*") // Chống lỗi CORS cho JS
public class HotelController {

    @Autowired
    private HotelService hotelService;

    // Phục vụ cho hàm fetchHotels() khi vừa load trang
    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotelsForDashboard());
    }

    // Nhớ import org.springframework.web.bind.annotation.RequestParam;

    @GetMapping("/search")
    public ResponseEntity<List<HotelDto>> searchHotels(
            @RequestParam(value = "location", required = false, defaultValue = "") String location,
            @RequestParam(value = "checkIn", required = false) LocalDate checkIn,
            @RequestParam(value = "checkOut", required = false) LocalDate checkOut) {
        
        List<HotelDto> hotels = hotelService.searchHotels(location, checkIn, checkOut);
        return ResponseEntity.ok(hotels);
    }

    // Nhớ import java.time.LocalDate; và
    // org.springframework.web.bind.annotation.RequestParam;

    @GetMapping("/{id}")
    public ResponseEntity<HotelDetailResponse> getHotelDetails(
            @PathVariable("id") Integer id,
            @RequestParam(value = "checkIn", required = false) LocalDate checkIn,
            @RequestParam(value = "checkOut", required = false) LocalDate checkOut) {

        try {
            // Truyền id, checkIn, checkOut xuống Service
            HotelDetailResponse response = hotelService.getHotelFullDetail(id, checkIn, checkOut);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            HotelDetailResponse errorResponse = new HotelDetailResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Không tìm thấy khách sạn: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}