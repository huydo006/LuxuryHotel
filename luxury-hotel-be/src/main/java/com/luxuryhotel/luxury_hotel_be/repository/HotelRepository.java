package com.luxuryhotel.luxury_hotel_be.repository;

import com.luxuryhotel.luxury_hotel_be.entity.Hotel;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Integer> {
    @Query(value = "SELECT DISTINCT h.* FROM hotels h " +
            "JOIN rooms r ON h.hotelID = r.hotelID " +
            "WHERE h.address LIKE CONCAT('%', :location, '%') " +
            "AND r.quantity > (" +
            "    SELECT COUNT(bd.bookingDetailID) FROM booking_details bd " +
            "    JOIN bookings b ON bd.bookingID = b.bookingID " +
            "    WHERE bd.roomID = r.roomID " +
            "    AND b.status IN ('processing', 'success') " +
            "    AND b.checkInDate < :checkOut " +
            "    AND b.checkOutDate > :checkIn" +
            ")", nativeQuery = true)
    List<Hotel> searchAvailableHotels(
            @Param("location") String location,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}