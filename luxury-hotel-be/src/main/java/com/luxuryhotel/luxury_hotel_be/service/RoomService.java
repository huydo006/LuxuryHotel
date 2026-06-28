package com.luxuryhotel.luxury_hotel_be.service;

import com.luxuryhotel.luxury_hotel_be.dto.AdminRoomDto;
import com.luxuryhotel.luxury_hotel_be.dto.BookedDateDto;
import com.luxuryhotel.luxury_hotel_be.dto.RoomDto;
import com.luxuryhotel.luxury_hotel_be.dto.RoomRequest;
import com.luxuryhotel.luxury_hotel_be.entity.Account; // <-- ĐÃ THÊM
import com.luxuryhotel.luxury_hotel_be.entity.Hotel;
import com.luxuryhotel.luxury_hotel_be.entity.Room;
import com.luxuryhotel.luxury_hotel_be.repository.AccountRepository; // <-- ĐÃ THÊM
import com.luxuryhotel.luxury_hotel_be.repository.BookingRepository;
import com.luxuryhotel.luxury_hotel_be.repository.HotelRepository;
import com.luxuryhotel.luxury_hotel_be.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // --- ĐÃ THÊM ACCOUNT REPOSITORY TẠI ĐÂY ---
    @Autowired
    private AccountRepository accountRepository;
    // ------------------------------------------

    public List<RoomDto> getAvailableRooms(Integer hotelId, LocalDate checkIn, LocalDate checkOut) {
        // Lấy dữ liệu thô từ Database
        List<RoomRepository.RoomAvailability> rawRooms = roomRepository.findAvailableRooms(hotelId, checkIn, checkOut);

        // Map sang DTO để trả về cho Frontend
        return rawRooms.stream().map(r -> {
            RoomDto dto = new RoomDto();
            dto.setRoomId(r.getRoomId());
            dto.setRoomType(r.getRoomType());
            dto.setCapacity(r.getCapacity());
            dto.setDefaultPrice(r.getDefaultPrice());
            dto.setAvailableQuantity(r.getAvailableQuantity());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<BookedDateDto> getBookedDatesByRoomId(Integer roomId) {
        // 1. Lấy tổng quỹ phòng (quantity)
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));
        int quantity = room.getQuantity();

        // 2. Lấy các khoảng thời gian đã được đặt của phòng này
        List<Object[]> bookingDates = bookingRepository.findActiveBookingsDatesByRoomId(roomId);

        // 3. Đếm số người đặt cho từng ngày (Map<Ngày, Số_lượng_người_đặt>)
        Map<LocalDate, Integer> dailyOverlapCounts = new HashMap<>();

        for (Object[] dates : bookingDates) {
            LocalDate checkIn = (LocalDate) dates[0];
            LocalDate checkOut = (LocalDate) dates[1];

            // Tăng biến đếm cho từng ngày từ checkIn đến TRƯỚC ngày checkOut
            for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
                dailyOverlapCounts.put(date, dailyOverlapCounts.getOrDefault(date, 0) + 1);
            }
        }

        // 4. Lọc ra danh sách NHỮNG NGÀY ĐÃ KÍN PHÒNG (count >= quantity) và Sắp xếp tăng dần
        List<LocalDate> fullyBookedDates = dailyOverlapCounts.entrySet().stream()
                .filter(entry -> entry.getValue() >= quantity)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());

        // 5. Gom các ngày liên tiếp thành các dải ngày (Ranges)
        List<BookedDateDto> result = new ArrayList<>();
        if (fullyBookedDates.isEmpty()) {
            return result; 
        }

        LocalDate startRange = fullyBookedDates.get(0);
        LocalDate prevDate = fullyBookedDates.get(0);

        for (int i = 1; i < fullyBookedDates.size(); i++) {
            LocalDate currDate = fullyBookedDates.get(i);

            if (currDate.equals(prevDate.plusDays(1))) {
                prevDate = currDate; 
            } else {
                BookedDateDto dto = new BookedDateDto();
                dto.setFrom(startRange.toString());
                dto.setTo(prevDate.toString());
                result.add(dto);

                startRange = currDate;
                prevDate = currDate;
            }
        }

        BookedDateDto lastDto = new BookedDateDto();
        lastDto.setFrom(startRange.toString());
        lastDto.setTo(prevDate.toString());
        result.add(lastDto);

        return result;
    }

    // 1. Hàm THÊM MỚI phòng
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createRoom(RoomRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Tìm khách sạn chủ quản
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách sạn này!"));

            Room room = new Room();
            room.setHotel(hotel);
            room.setRoomType(request.getRoomType());
            room.setCapacity(request.getCapacity());
            room.setDefaultPrice(request.getPrice());
            room.setQuantity(request.getQuantity());

            // ==========================================
            // LOGIC MỚI: LƯU VẾT NGƯỜI TẠO PHÒNG (AUDITING)
            // ==========================================
            if (request.getAdminId() != null) {
                Account admin = accountRepository.findById(request.getAdminId()).orElse(null);
                room.setCreatedBy(admin);
            }
            // ==========================================

            roomRepository.save(room);

            response.put("success", true);
            response.put("message", "Thêm phòng mới thành công!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi khi thêm phòng: " + e.getMessage());
        }
        return response;
    }

    // 2. Hàm SỬA thông tin phòng
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateRoom(Integer id, RoomRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Room room = roomRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng có ID: " + id));

            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách sạn!"));

            room.setHotel(hotel);
            room.setRoomType(request.getRoomType());
            room.setCapacity(request.getCapacity());
            room.setDefaultPrice(request.getPrice());
            room.setQuantity(request.getQuantity());

            roomRepository.save(room);

            response.put("success", true);
            response.put("message", "Cập nhật phòng thành công!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật phòng: " + e.getMessage());
        }
        return response;
    }

    // 3. Hàm XÓA phòng
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteRoom(Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!roomRepository.existsById(id)) {
                response.put("success", false);
                response.put("message", "Không tìm thấy phòng để xóa!");
                return response;
            }
            roomRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Đã xóa phòng thành công!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi khi xóa phòng: " + e.getMessage());
        }
        return response;
    }

    // Hàm lấy danh sách phòng theo ID Khách sạn cho Admin
    public List<AdminRoomDto> getRoomsByHotelId(Integer hotelId) {
        return roomRepository.findByHotel_HotelId(hotelId).stream().map(room -> {
            AdminRoomDto dto = new AdminRoomDto();
            dto.setRoomID(room.getRoomId());
            dto.setRoomType(room.getRoomType());
            dto.setCapacity(room.getCapacity());
            dto.setDefaultPrice(room.getDefaultPrice());
            dto.setQuantity(room.getQuantity());
            return dto;
        }).collect(Collectors.toList());
    }
}