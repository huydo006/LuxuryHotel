package com.luxuryhotel.luxury_hotel_be.service;

import com.luxuryhotel.luxury_hotel_be.dto.HotelDetailResponse;
import com.luxuryhotel.luxury_hotel_be.dto.HotelDto;
import com.luxuryhotel.luxury_hotel_be.dto.RoomSimpleDto;
import com.luxuryhotel.luxury_hotel_be.entity.Hotel;
import com.luxuryhotel.luxury_hotel_be.repository.HotelRepository;
import com.luxuryhotel.luxury_hotel_be.repository.RoomRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
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
}