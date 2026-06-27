package com.luxuryhotel.luxury_hotel_be.repository;

import com.luxuryhotel.luxury_hotel_be.entity.BookingDetail;

import java.util.List;
import java.util.Optional;

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

       // Thêm hàm này vào dưới hàm findHistoryByAccountId cũ
       @Query("SELECT bd FROM BookingDetail bd " +
                     "JOIN FETCH bd.booking b " +
                     "JOIN FETCH b.account a " + // JOIN thêm bảng Account để lấy username
                     "JOIN FETCH bd.room r " +
                     "JOIN FETCH r.hotel h " +
                     "ORDER BY b.createdAt DESC")
       List<BookingDetail> findAllBookingsForAdmin();

       // BookingDetailRepository.java
       @Query("SELECT bd FROM BookingDetail bd WHERE bd.booking.bookingId = :bookingId")
       Optional<BookingDetail> findByBookingId(@Param("bookingId") Integer bookingId);
}