package com.luxuryhotel.luxury_hotel_be.controller;

import com.luxuryhotel.luxury_hotel_be.dto.BookedDateDto;
import com.luxuryhotel.luxury_hotel_be.dto.RoomDto;
import com.luxuryhotel.luxury_hotel_be.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired
    private RoomService roomService;
    

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }


    @GetMapping("/available")
    public ResponseEntity<List<RoomDto>> getAvailableRooms(
            @RequestParam("hotelId") Integer hotelId,
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        List<RoomDto> rooms = roomService.getAvailableRooms(hotelId, checkIn, checkOut);
        return ResponseEntity.ok(rooms);
    }


    @GetMapping("/{roomId}/booked-dates")
    public ResponseEntity<List<BookedDateDto>> getBookedDates(@PathVariable("roomId") Integer roomId) {
        // Gọi Service truy vấn bảng Booking/BookingDetail để tìm các đơn đang 'success' hoặc 'processing'
        // Map ngày checkInDate thành 'from', checkOutDate thành 'to'
        
        List<BookedDateDto> bookedDates = roomService.getBookedDatesByRoomId(roomId);
        return ResponseEntity.ok(bookedDates);
    }
}