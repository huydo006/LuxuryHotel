package com.luxuryhotel.luxury_hotel_be.controller;

import com.luxuryhotel.luxury_hotel_be.dto.BookingAdminDto;
import com.luxuryhotel.luxury_hotel_be.dto.BookingHistoryDto;
import com.luxuryhotel.luxury_hotel_be.dto.BookingRequest;
import com.luxuryhotel.luxury_hotel_be.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody BookingRequest request) {
        Map<String, Object> response = bookingService.createBooking(request);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Lấy lịch sử đặt phòng
    @GetMapping("/user/{accountId}")
    public ResponseEntity<List<BookingHistoryDto>> getBookingHistory(@PathVariable("accountId") Integer accountId) {
        return ResponseEntity.ok(bookingService.getBookingHistory(accountId));
    }

    // Hủy đơn đặt phòng
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable("id") Integer id) {
        Map<String, Object> response = bookingService.cancelBooking(id);
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API 1: Lấy tất cả đơn hàng (JS gọi GET /api/bookings/all)
    @GetMapping("/all")
    public ResponseEntity<List<BookingAdminDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookingsForAdmin());
    }

    // API 2: Admin Duyệt/Hủy đơn hàng (JS gọi PUT /api/bookings/{id}/status)
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable("id") Integer id, 
            @RequestBody Map<String, String> requestBody) { // Hứng chuỗi JSON { "status": "success" }
        
        String newStatus = requestBody.get("status");
        Map<String, Object> response = bookingService.updateBookingStatus(id, newStatus);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}