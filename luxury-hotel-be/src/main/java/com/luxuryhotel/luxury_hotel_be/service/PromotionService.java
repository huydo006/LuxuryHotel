package com.luxuryhotel.luxury_hotel_be.service;

import com.luxuryhotel.luxury_hotel_be.dto.PromoApplyRequest;
import com.luxuryhotel.luxury_hotel_be.dto.PromotionDto;
import com.luxuryhotel.luxury_hotel_be.dto.PromotionRequest;
import com.luxuryhotel.luxury_hotel_be.entity.Account; 
import com.luxuryhotel.luxury_hotel_be.entity.Promotion;
import com.luxuryhotel.luxury_hotel_be.repository.AccountRepository; 
import com.luxuryhotel.luxury_hotel_be.repository.BookingRepository; // <-- ĐÃ THÊM IMPORT NÀY
import com.luxuryhotel.luxury_hotel_be.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private AccountRepository accountRepository;

    // --- ĐÃ THÊM BOOKING REPOSITORY TẠI ĐÂY ---
    @Autowired
    private BookingRepository bookingRepository;
    // ------------------------------------------

    // ============================================
    // PHẦN DÀNH CHO ADMIN
    // ============================================

    public List<PromotionDto> getAllPromotions() {
        return promotionRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPromotion(PromotionRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        if (promotionRepository.findByDiscountCode(request.getDiscountCode()).isPresent()) {
            response.put("success", false);
            response.put("message", "Mã khuyến mãi (Code) này đã tồn tại!");
            return response;
        }

        Promotion p = new Promotion();
        p.setDiscountCode(request.getDiscountCode());
        p.setNamePromo(request.getNamePromo());
        p.setDiscountPercent(request.getDiscountPercent());
        p.setMaxDiscountAmount(request.getMaxDiscountAmount());
        p.setMinBookingValue(request.getMinBookingValue());
        p.setUsageLimit(request.getUsageLimit());
        p.setStartDate(request.getStartDate());
        p.setEndDate(request.getEndDate());
        p.setUsedCount(0);
        p.setIsValid(1); // Mặc định là bật

        if (request.getAdminId() != null) {
            Account admin = accountRepository.findById(request.getAdminId()).orElse(null);
            p.setCreatedBy(admin);
        }

        promotionRepository.save(p);
        response.put("success", true);
        response.put("message", "Tạo mã khuyến mãi thành công!");
        return response;
    }

    @Transactional
    public Map<String, Object> togglePromotionStatus(Integer id, Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        Promotion p = promotionRepository.findById(id).orElse(null);
        if (p == null) {
            response.put("success", false);
            response.put("message", "Không tìm thấy khuyến mãi!");
            return response;
        }
        
        p.setIsValid(request.get("isValid"));
        promotionRepository.save(p);
        response.put("success", true);
        response.put("message", "Đã cập nhật trạng thái khuyến mãi!");
        return response;
    }

    @Transactional
    public Map<String, Object> deletePromotion(Integer id) {
        Map<String, Object> response = new HashMap<>();
        promotionRepository.deleteById(id);
        response.put("success", true);
        response.put("message", "Xóa khuyến mãi thành công!");
        return response;
    }

    // ============================================
    // PHẦN DÀNH CHO CUSTOMER
    // ============================================

    public List<PromotionDto> getAvailablePromotions() {
        return promotionRepository.findAvailablePromotions().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public Map<String, Object> applyPromotion(PromoApplyRequest request) {
        Map<String, Object> response = new HashMap<>();
        Optional<Promotion> promoOpt = promotionRepository.findByDiscountCode(request.getDiscountCode());

        if (promoOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Mã giảm giá không tồn tại!");
            return response;
        }

        Promotion p = promoOpt.get();
        LocalDate today = LocalDate.now();

        // Kiểm tra các điều kiện
        if (p.getIsValid() == 0) {
            response.put("success", false);
            response.put("message", "Mã giảm giá đang bị tạm dừng!");
            return response;
        }
        if (today.isBefore(p.getStartDate()) || today.isAfter(p.getEndDate())) {
            response.put("success", false);
            response.put("message", "Mã giảm giá không trong thời gian áp dụng!");
            return response;
        }
        if (p.getUsedCount() >= p.getUsageLimit()) {
            response.put("success", false);
            response.put("message", "Mã giảm giá đã hết lượt sử dụng!");
            return response;
        }
        if (request.getBookingTotal() < p.getMinBookingValue()) {
            response.put("success", false);
            response.put("message", "Đơn hàng chưa đạt giá trị tối thiểu (" + p.getMinBookingValue() + "đ) để dùng mã này!");
            return response;
        }

        // ==========================================
        // LOGIC MỚI: CHẶN DÙNG MÃ NHIỀU LẦN
        // ==========================================
        if (request.getUserId() != null) {
            boolean alreadyUsed = bookingRepository.hasUserUsedPromotion(request.getUserId(), p.getPromotionId());
            if (alreadyUsed) {
                response.put("success", false);
                response.put("message", "Bạn đã sử dụng mã khuyến mãi này cho một đơn đặt phòng khác rồi!");
                return response;
            }
        }
        // ==========================================

        // Tính toán số tiền được giảm
        double calculatedDiscount = (request.getBookingTotal() * p.getDiscountPercent()) / 100;
        double finalDiscount = Math.min(calculatedDiscount, p.getMaxDiscountAmount());

        response.put("success", true);
        response.put("message", "Áp dụng mã thành công!");
        response.put("promotionID", p.getPromotionId());
        response.put("discountAmount", finalDiscount);
        return response;
    }

    // Hàm phụ trợ convert Entity sang DTO
    private PromotionDto mapToDto(Promotion p) {
        PromotionDto dto = new PromotionDto();
        dto.setPromotionID(p.getPromotionId());
        dto.setDiscountCode(p.getDiscountCode());
        dto.setNamePromo(p.getNamePromo());
        dto.setDiscountPercent(p.getDiscountPercent());
        dto.setMaxDiscountAmount(p.getMaxDiscountAmount());
        dto.setMinBookingValue(p.getMinBookingValue());
        dto.setUsageLimit(p.getUsageLimit());
        dto.setUsedCount(p.getUsedCount());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setIsValid(p.getIsValid());
        return dto;
    }
}