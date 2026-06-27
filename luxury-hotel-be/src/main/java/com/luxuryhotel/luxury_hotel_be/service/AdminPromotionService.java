package com.luxuryhotel.luxury_hotel_be.service;

import com.luxuryhotel.luxury_hotel_be.dto.PromotionRequest;
import com.luxuryhotel.luxury_hotel_be.entity.Promotion;
import com.luxuryhotel.luxury_hotel_be.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminPromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPromotion(PromotionRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Promotion promo = new Promotion();
            promo.setDiscountCode(request.getDiscountCode().toUpperCase());
            promo.setNamePromo(request.getNamePromo());
            promo.setDiscountPercent(request.getDiscountPercent());
            promo.setMaxDiscountAmount(request.getMaxDiscountAmount());
            promo.setMinBookingValue(request.getMinBookingValue());
            promo.setUsageLimit(request.getUsageLimit());
            promo.setUsedCount(0); // Mặc định chưa ai dùng
            promo.setStartDate(request.getStartDate());
            promo.setEndDate(request.getEndDate());
            promo.setIsValid(1); // Mặc định kích hoạt

            promotionRepository.save(promo);
            response.put("success", true);
            response.put("message", "Thêm mã khuyến mãi thành công!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi (Có thể do trùng mã Code): " + e.getMessage());
        }
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> togglePromotionStatus(Integer id, Integer isValid) {
        Map<String, Object> response = new HashMap<>();
        try {
            Promotion promo = promotionRepository.findById(id).orElseThrow();
            promo.setIsValid(isValid);
            promotionRepository.save(promo);
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deletePromotion(Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            promotionRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Đã xóa vĩnh viễn mã khuyến mãi!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }
}