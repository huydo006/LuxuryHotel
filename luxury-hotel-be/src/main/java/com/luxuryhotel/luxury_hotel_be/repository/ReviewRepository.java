package com.luxuryhotel.luxury_hotel_be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.luxuryhotel.luxury_hotel_be.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByHotel_HotelId(Integer hotelId);
}
