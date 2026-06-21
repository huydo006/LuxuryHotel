package com.luxuryhotel.luxury_hotel_be.repository;

import com.luxuryhotel.luxury_hotel_be.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // Đã sửa lỗi: Dùng JOIN ngược từ BookingDetail (chứa biến booking) sang Booking
    @Query("SELECT b.checkInDate, b.checkOutDate FROM BookingDetail bd JOIN bd.booking b " +
           "WHERE bd.room.roomId = :roomId " +
           "AND b.status IN ('processing', 'success') " +
           "AND b.checkOutDate >= CURRENT_DATE")
    List<Object[]> findActiveBookingsDatesByRoomId(@Param("roomId") Integer roomId);
}