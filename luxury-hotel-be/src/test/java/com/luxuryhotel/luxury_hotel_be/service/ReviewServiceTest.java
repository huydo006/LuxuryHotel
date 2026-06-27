package com.luxuryhotel.luxury_hotel_be.service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.luxuryhotel.luxury_hotel_be.dto.ReviewRequest;
import com.luxuryhotel.luxury_hotel_be.entity.Account;
import com.luxuryhotel.luxury_hotel_be.entity.Hotel;
import com.luxuryhotel.luxury_hotel_be.entity.Review;
import com.luxuryhotel.luxury_hotel_be.repository.AccountRepository;
import com.luxuryhotel.luxury_hotel_be.repository.HotelRepository;
import com.luxuryhotel.luxury_hotel_be.repository.ReviewRepository;

class ReviewServiceTest {

    @Test
    void createReviewShouldSaveUploadedImages() throws Exception {
        ReviewRepository reviewRepository = mock(ReviewRepository.class);
        AccountRepository accountRepository = mock(AccountRepository.class);
        HotelRepository hotelRepository = mock(HotelRepository.class);

        ReviewService reviewService = new ReviewService();
        setField(reviewService, "reviewRepository", reviewRepository);
        setField(reviewService, "accountRepository", accountRepository);
        setField(reviewService, "hotelRepository", hotelRepository);
        setField(reviewService, "uploadDir", "target/test-uploads");

        Account account = new Account();
        account.setAccountId(1);
        Hotel hotel = new Hotel();
        hotel.setHotelId(2);
        hotel.setRating(5);

        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(hotelRepository.findById(2)).thenReturn(Optional.of(hotel));
        when(reviewRepository.findByHotel_HotelId(2)).thenReturn(List.of());

        MultipartFile[] files = {
                new MockMultipartFile("images", "photo.jpg", "image/jpeg", "image-bytes".getBytes())
        };

        Map<String, Object> response = reviewService.createReview(
                new ReviewRequest(1, 2, 5, "Phòng đẹp"),
                files
        );

        assertTrue((Boolean) response.get("success"));
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());

        Review savedReview = captor.getValue();
        assertEquals("Phòng đẹp", savedReview.getComment());
        assertTrue(savedReview.getImageUrls().contains("photo.jpg"));
    }

    @Test
    void createReviewShouldRejectUnsupportedImageType() throws Exception {
        ReviewRepository reviewRepository = mock(ReviewRepository.class);
        AccountRepository accountRepository = mock(AccountRepository.class);
        HotelRepository hotelRepository = mock(HotelRepository.class);

        ReviewService reviewService = new ReviewService();
        setField(reviewService, "reviewRepository", reviewRepository);
        setField(reviewService, "accountRepository", accountRepository);
        setField(reviewService, "hotelRepository", hotelRepository);
        setField(reviewService, "uploadDir", "target/test-uploads");

        Account account = new Account();
        account.setAccountId(1);
        Hotel hotel = new Hotel();
        hotel.setHotelId(2);
        hotel.setRating(5);

        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(hotelRepository.findById(2)).thenReturn(Optional.of(hotel));

        MultipartFile[] files = {
                new MockMultipartFile("images", "photo.gif", "image/gif", "image-bytes".getBytes())
        };

        Map<String, Object> response = reviewService.createReview(
                new ReviewRequest(1, 2, 5, "Phòng đẹp"),
                files
        );

        assertTrue(response.containsKey("success"));
        assertEquals(false, response.get("success"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
