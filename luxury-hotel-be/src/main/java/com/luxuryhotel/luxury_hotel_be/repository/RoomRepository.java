package com.luxuryhotel.luxury_hotel_be.repository;

import com.luxuryhotel.luxury_hotel_be.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    // 1. Dùng Interface Projection để hứng kết quả trả về từ Native Query
    public interface RoomAvailability {
        Integer getRoomId();
        String getRoomType();
        Integer getCapacity();
        Double getDefaultPrice();
        Integer getAvailableQuantity(); // Cột tính toán động
    }

    // 2. Query tính phòng trống: Số phòng trống = quantity - (số đơn trùng lịch)
    @Query(value = "SELECT r.roomID AS roomId, r.roomType AS roomType, r.capacity AS capacity, r.defaultPrice AS defaultPrice, " +
                   "(r.quantity - COALESCE(" +
                   "    (SELECT COUNT(bd.bookingDetailID) FROM booking_details bd " +
                   "     JOIN bookings b ON bd.bookingID = b.bookingID " +
                   "     WHERE bd.roomID = r.roomID " +
                   "     AND b.status IN ('processing', 'success') " +
                   "     AND b.checkInDate < :checkOut " +
                   "     AND b.checkOutDate > :checkIn" +
                   "    ), 0" +
                   ")) AS availableQuantity " +
                   "FROM rooms r " +
                   "WHERE r.hotelID = :hotelId " +
                   "HAVING availableQuantity > 0", nativeQuery = true)
    List<RoomAvailability> findAvailableRooms(
            @Param("hotelId") Integer hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    // 3. Tìm tất cả các phòng thuộc về 1 khách sạn
    List<Room> findByHotel_HotelId(Integer hotelId);

    
}