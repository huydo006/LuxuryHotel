package com.luxuryhotel.luxury_hotel_be.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.luxuryhotel.luxury_hotel_be.dto.ReviewDto;
import com.luxuryhotel.luxury_hotel_be.dto.ReviewRequest;
import com.luxuryhotel.luxury_hotel_be.entity.Account;
import com.luxuryhotel.luxury_hotel_be.entity.Hotel;
import com.luxuryhotel.luxury_hotel_be.entity.Review;
import com.luxuryhotel.luxury_hotel_be.repository.AccountRepository;
import com.luxuryhotel.luxury_hotel_be.repository.HotelRepository;
import com.luxuryhotel.luxury_hotel_be.repository.ReviewRepository;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Value("${app.upload-dir:uploads/reviews}")
    private String uploadDir;

    public Map<String, Object> createReview(ReviewRequest request, MultipartFile[] images) {
        Map<String, Object> response = new HashMap<>();
        try {
            Account account = accountRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách sạn"));

            Review review = new Review();
            review.setAccount(account);
            review.setHotel(hotel);
            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setCreatedAt(LocalDateTime.now());

            List<String> imageUrls = new ArrayList<>();
            if (images != null) {
                for (MultipartFile image : images) {
                    if (image != null && !image.isEmpty()) {
                        validateImage(image);
                        String fileName = saveImage(image);
                        imageUrls.add(fileName);
                    }
                }
            }
            review.setImageUrls(String.join(",", imageUrls));

            reviewRepository.save(review);

            response.put("success", true);
            response.put("message", "Đánh giá thành công!");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    public List<ReviewDto> getReviewsByHotel(Integer hotelId) {
        return reviewRepository.findByHotel_HotelId(hotelId).stream().map(review -> {
            ReviewDto dto = new ReviewDto();
            dto.setUsername(review.getAccount().getUsername());
            dto.setRating(review.getRating());
            dto.setComment(review.getComment());
            dto.setCreatedAt(review.getCreatedAt());
            dto.setImageUrls(review.getImageUrls());
            return dto;
        }).collect(Collectors.toList());
    }

    private void validateImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Chỉ hỗ trợ ảnh JPG, PNG hoặc WEBP");
        }
    }

    private String saveImage(MultipartFile file) throws IOException {
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }
        
        // SỬA Ở ĐÂY: Luôn dùng UUID ngẫu nhiên để làm tên file, tránh trùng lặp 100%
        String fileName = UUID.randomUUID().toString() + extension;

        Path target = dir.resolve(fileName);
        
        // Thêm StandardCopyOption.REPLACE_EXISTING để ghi đè an toàn
        java.nio.file.Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        return fileName;
    }
}
