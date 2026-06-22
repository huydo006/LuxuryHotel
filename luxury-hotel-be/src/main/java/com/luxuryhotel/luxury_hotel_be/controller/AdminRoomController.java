package com.luxuryhotel.luxury_hotel_be.controller;

import com.luxuryhotel.luxury_hotel_be.dto.RoomRequest;
import com.luxuryhotel.luxury_hotel_be.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/rooms")
@CrossOrigin(origins = "*")
public class AdminRoomController {

    @Autowired
    private RoomService roomService;

    // API Thêm phòng mới (POST)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRoom(@RequestBody RoomRequest request) {
        Map<String, Object> response = roomService.createRoom(request);
        return ResponseEntity.ok(response);
    }

    // API Sửa thông tin phòng (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRoom(
            @PathVariable Integer id, 
            @RequestBody RoomRequest request) {
        Map<String, Object> response = roomService.updateRoom(id, request);
        return ResponseEntity.ok(response);
    }
}