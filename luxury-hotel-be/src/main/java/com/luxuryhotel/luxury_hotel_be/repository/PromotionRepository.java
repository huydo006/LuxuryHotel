package com.luxuryhotel.luxury_hotel_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.luxuryhotel.luxury_hotel_be.entity.Promotion;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    
    // Tìm mã khuyến mãi theo Code
    Optional<Promotion> findByDiscountCode(String discountCode);

    // Tìm các mã đang hợp lệ (Đang bật, Trong thời gian áp dụng, Chưa hết lượt)
    @Query("SELECT p FROM Promotion p WHERE p.isValid = 1 " +
           "AND p.startDate <= CURRENT_DATE " +
           "AND p.endDate >= CURRENT_DATE " +
           "AND p.usedCount < p.usageLimit")
    List<Promotion> findAvailablePromotions();
}