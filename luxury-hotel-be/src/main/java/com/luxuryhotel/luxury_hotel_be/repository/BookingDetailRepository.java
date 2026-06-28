package com.luxuryhotel.luxury_hotel_be.repository;

import com.luxuryhotel.luxury_hotel_be.entity.BookingDetail;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Integer> {
    
    @Query("SELECT bd FROM BookingDetail bd " +
           "JOIN FETCH bd.booking b " +
           "JOIN FETCH bd.room r " +
           "JOIN FETCH r.hotel h " +
           "WHERE b.account.accountId = :accountId " +
           "ORDER BY b.createdAt DESC")
    List<BookingDetail> findHistoryByAccountId(@Param("accountId") Integer accountId);

    // --- SỬA LỖI TẠI ĐÂY: DÙNG 'LEFT JOIN' CHO BẢNG ACCOUNT ---
    @Query("SELECT bd FROM BookingDetail bd " +
           "JOIN FETCH bd.booking b " +
           "LEFT JOIN FETCH b.account a " + // LEFT JOIN để lấy cả đơn của khách đã bị xóa
           "JOIN FETCH bd.room r " +
           "JOIN FETCH r.hotel h " +
           "ORDER BY b.createdAt DESC")
    List<BookingDetail> findAllBookingsForAdmin();
    // ------------------------------------------------------------

    List<BookingDetail> findByBooking_BookingId(Integer bookingId);
}