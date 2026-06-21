package com.luxuryhotel.luxury_hotel_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.luxuryhotel.luxury_hotel_be.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    
}
