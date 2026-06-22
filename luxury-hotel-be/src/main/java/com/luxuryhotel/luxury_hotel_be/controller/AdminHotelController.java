package com.luxuryhotel.luxury_hotel_be.controller;

import com.luxuryhotel.luxury_hotel_be.dto.AdminRoomDto;
import com.luxuryhotel.luxury_hotel_be.dto.HotelRequest;
import com.luxuryhotel.luxury_hotel_be.service.HotelService;
import com.luxuryhotel.luxury_hotel_be.service.RoomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/hotels")
@CrossOrigin(origins = "*") // Đảm bảo Frontend ở port khác có thể gọi được
public class AdminHotelController {

    @Autowired
    private HotelService hotelService;

    // API Thêm khách sạn mới (POST)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createHotel(@RequestBody HotelRequest request) {
        Map<String, Object> response = hotelService.createHotel(request);
        return ResponseEntity.ok(response);
    }

    // API Sửa thông tin khách sạn (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateHotel(
            @PathVariable Integer id, 
            @RequestBody HotelRequest request) {
        Map<String, Object> response = hotelService.updateHotel(id, request);
        return ResponseEntity.ok(response);
    }
    
    // API Xóa khách sạn (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteHotel(@PathVariable Integer id) {
        Map<String, Object> response = hotelService.deleteHotel(id);
        return ResponseEntity.ok(response);
    }

    // 1. Nhớ tiêm (Autowired) thêm RoomService ở đầu class
    @Autowired
    private RoomService roomService;

    // 2. Thêm API lấy danh sách phòng của 1 khách sạn
    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<AdminRoomDto>> getRoomsByHotel(@PathVariable Integer hotelId) {
        List<AdminRoomDto> rooms = roomService.getRoomsByHotelId(hotelId);
        return ResponseEntity.ok(rooms);
    }
}