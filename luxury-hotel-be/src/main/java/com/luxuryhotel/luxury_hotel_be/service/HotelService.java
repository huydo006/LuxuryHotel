package com.luxuryhotel.luxury_hotel_be.service;

import com.luxuryhotel.luxury_hotel_be.dto.HotelDetailResponse;
import com.luxuryhotel.luxury_hotel_be.dto.HotelDto;
import com.luxuryhotel.luxury_hotel_be.dto.HotelRequest;
import com.luxuryhotel.luxury_hotel_be.dto.RoomSimpleDto;
import com.luxuryhotel.luxury_hotel_be.entity.Hotel;
import com.luxuryhotel.luxury_hotel_be.repository.HotelRepository;
import com.luxuryhotel.luxury_hotel_be.repository.RoomRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;
    
    @Autowired
    private RoomRepository roomRepository;

    public List<HotelDto> getAllHotelsForDashboard() {
        // Lấy toàn bộ Entity từ DB
        List<Hotel> hotels = hotelRepository.findAll();

        // Dùng Stream API của Java để map Entity sang DTO cực gọn
        return hotels.stream().map(hotel -> {
            HotelDto dto = new HotelDto();
            dto.setId(hotel.getHotelId());
            dto.setName(hotel.getNameHotel());
            dto.setLocation(hotel.getAddress()); // Map address -> location cho JS đọc
            dto.setRating(hotel.getRating());
            dto.setDescription(hotel.getDescription());
            dto.setImage(hotel.getImage());
            dto.setBookingsCount(hotel.getBookingsCount());

            // --- TÌM SỨC CHỨA LỚN NHẤT CỦA KHÁCH SẠN ---
            Integer maxCap = roomRepository.findByHotel_HotelId(hotel.getHotelId())
                    .stream()
                    .mapToInt(room -> room.getCapacity())
                    .max()
                    .orElse(0); 
            dto.setMaxCapacity(maxCap);
            // -------------------------------------------

            return dto;
        }).collect(Collectors.toList());
    }

    public List<HotelDto> searchHotels(String location, LocalDate checkIn, LocalDate checkOut) {
        // LUỒNG MẶC ĐỊNH: Nếu người dùng để trống lịch, tự động lấy Hôm nay -> Ngày mai
        LocalDate start = (checkIn != null) ? checkIn : LocalDate.now();
        LocalDate end = (checkOut != null) ? checkOut : LocalDate.now().plusDays(1);

        // Gọi Query trong Repository (đã có sẵn BR3 chống trùng phòng)
        List<Hotel> availableHotels = hotelRepository.searchAvailableHotels(location, start, end);

        // Convert sang DTO
        return availableHotels.stream().map(hotel -> {
            HotelDto dto = new HotelDto();
            dto.setId(hotel.getHotelId());
            dto.setName(hotel.getNameHotel());
            dto.setLocation(hotel.getAddress());
            dto.setRating(hotel.getRating());
            dto.setDescription(hotel.getDescription());
            dto.setImage(hotel.getImage());
            dto.setBookingsCount(hotel.getBookingsCount());

            // --- TÌM SỨC CHỨA LỚN NHẤT CỦA KHÁCH SẠN ---
            Integer maxCap = roomRepository.findByHotel_HotelId(hotel.getHotelId())
                    .stream()
                    .mapToInt(room -> room.getCapacity())
                    .max()
                    .orElse(0);
            dto.setMaxCapacity(maxCap);
            // -------------------------------------------

            return dto;
        }).collect(Collectors.toList());
    }

    public HotelDetailResponse getHotelFullDetail(Integer hotelId, LocalDate checkIn, LocalDate checkOut) {
        HotelDetailResponse response = new HotelDetailResponse();

        // 1. Lấy Hotel Entity và map sang DTO
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        HotelDto hotelDto = new HotelDto();
        hotelDto.setId(hotel.getHotelId());
        hotelDto.setName(hotel.getNameHotel());
        hotelDto.setLocation(hotel.getAddress());
        hotelDto.setRating(hotel.getRating());
        hotelDto.setDescription(hotel.getDescription());
        hotelDto.setImage(hotel.getImage());
        hotelDto.setBookingsCount(hotel.getBookingsCount());

        // 2. LẤY DANH SÁCH PHÒNG VÀ TÍNH TOÁN SỐ LƯỢNG TRỐNG
        List<RoomSimpleDto> rooms;

        if (checkIn != null && checkOut != null) {
            // Trường hợp 1: Khách có lọc ngày -> Dùng Query BR3 tính chính xác số phòng trống
            List<RoomRepository.RoomAvailability> availableRooms = roomRepository.findAvailableRooms(hotelId, checkIn, checkOut);
            rooms = availableRooms.stream().map(r -> {
                RoomSimpleDto dto = new RoomSimpleDto();
                dto.setId(r.getRoomId());
                dto.setName(r.getRoomType());
                dto.setCapacity(r.getCapacity());
                dto.setPrice(r.getDefaultPrice());
                dto.setAvailableQuantity(r.getAvailableQuantity()); // Số phòng trống thực tế
                return dto;
            }).collect(Collectors.toList());
        } else {
            // Trường hợp 2: Khách xem trực tiếp không lọc ngày -> Hiển thị tổng quỹ phòng
            rooms = roomRepository.findByHotel_HotelId(hotelId).stream().map(r -> {
                RoomSimpleDto dto = new RoomSimpleDto();
                dto.setId(r.getRoomId());
                dto.setName(r.getRoomType());
                dto.setCapacity(r.getCapacity()); 
                dto.setPrice(r.getDefaultPrice());
                dto.setAvailableQuantity(r.getQuantity()); // Lấy tổng quỹ phòng
                return dto;
            }).collect(Collectors.toList());
        }

        // 3. Set vào response
        response.setSuccess(true);
        response.setHotel(hotelDto);
        response.setRooms(rooms);
        // response.setReviews(reviewList); // Làm tương tự với Reviews nếu em đã viết

        return response;
    }

    // Nhớ import cái này ở đầu file nhé:
    // import org.springframework.transaction.interceptor.TransactionAspectSupport;

    // 1. Hàm THÊM MỚI khách sạn
    @Transactional(rollbackFor = Exception.class) 
    public Map<String, Object> createHotel(HotelRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Hotel hotel = new Hotel();
            hotel.setNameHotel(request.getName());
            hotel.setAddress(request.getLocation()); // Frontend gọi là location, DB là address
            hotel.setImage(request.getImage());
            hotel.setDescription(request.getDescription());
            
            // Thiết lập giá trị mặc định cho khách sạn mới
            hotel.setRating(5); // Khách sạn Luxury mặc định 5 sao
            hotel.setBookingsCount(0); // Lượt đặt ban đầu là 0

            hotelRepository.save(hotel);

            response.put("success", true);
            response.put("message", "Thêm khách sạn mới thành công!");
        } catch (Exception e) {
            // Lệnh ép Spring phải Rollback Database dù đã dùng try-catch
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            
            response.put("success", false);
            response.put("message", "Lỗi khi thêm khách sạn: " + e.getMessage());
        }
        return response;
    }

    // 2. Hàm SỬA khách sạn
    @Transactional(rollbackFor = Exception.class) // Đổi rollbackOn -> rollbackFor
    public Map<String, Object> updateHotel(Integer id, HotelRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Hotel hotel = hotelRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách sạn có ID: " + id));

            // Cập nhật thông tin mới
            hotel.setNameHotel(request.getName());
            hotel.setAddress(request.getLocation());
            hotel.setImage(request.getImage());
            hotel.setDescription(request.getDescription());

            hotelRepository.save(hotel);

            response.put("success", true);
            response.put("message", "Cập nhật thông tin khách sạn thành công!");
        } catch (Exception e) {
            // Lệnh ép Spring phải Rollback Database
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật: " + e.getMessage());
        }
        return response;
    }
    // 3. Hàm XÓA khách sạn
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteHotel(Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Kiểm tra xem khách sạn có tồn tại không
            if (!hotelRepository.existsById(id)) {
                response.put("success", false);
                response.put("message", "Không tìm thấy khách sạn để xóa!");
                return response;
            }

            // Thực hiện xóa
            hotelRepository.deleteById(id);

            response.put("success", true);
            response.put("message", "Đã xóa khách sạn và các phòng liên quan thành công!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi khi xóa khách sạn: " + e.getMessage());
        }
        return response;
    }
}