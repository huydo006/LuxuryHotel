package com.luxuryhotel.luxury_hotel_be.service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.luxuryhotel.luxury_hotel_be.dto.BookingAdminDto;
import com.luxuryhotel.luxury_hotel_be.dto.BookingHistoryDto;
import com.luxuryhotel.luxury_hotel_be.dto.BookingRequest;
import com.luxuryhotel.luxury_hotel_be.entity.Account;
import com.luxuryhotel.luxury_hotel_be.entity.Booking;
import com.luxuryhotel.luxury_hotel_be.entity.BookingDetail;
import com.luxuryhotel.luxury_hotel_be.entity.Hotel;
import com.luxuryhotel.luxury_hotel_be.entity.Promotion;
import com.luxuryhotel.luxury_hotel_be.entity.Room;
import com.luxuryhotel.luxury_hotel_be.repository.AccountRepository;
import com.luxuryhotel.luxury_hotel_be.repository.BookingDetailRepository;
import com.luxuryhotel.luxury_hotel_be.repository.BookingRepository;
import com.luxuryhotel.luxury_hotel_be.repository.HotelRepository;
import com.luxuryhotel.luxury_hotel_be.repository.PromotionRepository;
import com.luxuryhotel.luxury_hotel_be.repository.RoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final RoomRepository roomRepository;
    private final AccountRepository accountRepository;
    private final PromotionRepository promotionRepository; // Thêm Repo Khuyến mãi
    private final HotelRepository hotelRepository;
    
    private final EmailService emailService;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createBooking(BookingRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Phòng không tồn tại!"));

            List<Object[]> activeBookings = bookingRepository.findActiveBookingsDatesByRoomId(request.getRoomId());
            Map<LocalDate, Integer> dailyOverlapCounts = new HashMap<>();

            for (Object[] dates : activeBookings) {
                LocalDate existingCheckIn = (LocalDate) dates[0];
                LocalDate existingCheckOut = (LocalDate) dates[1];

                if (existingCheckIn.isBefore(request.getCheckOutDate())
                        && existingCheckOut.isAfter(request.getCheckInDate())) {
                    LocalDate startOverlap = existingCheckIn.isAfter(request.getCheckInDate()) ? existingCheckIn
                            : request.getCheckInDate();
                    LocalDate endOverlap = existingCheckOut.isBefore(request.getCheckOutDate()) ? existingCheckOut
                            : request.getCheckOutDate();

                    for (LocalDate date = startOverlap; date.isBefore(endOverlap); date = date.plusDays(1)) {
                        dailyOverlapCounts.put(date, dailyOverlapCounts.getOrDefault(date, 0) + 1);

                        if (dailyOverlapCounts.get(date) >= room.getQuantity()) {
                            response.put("success", false);
                            response.put("message",
                                    "Rất tiếc, ngày " + date + " đã hết phòng. Vui lòng chọn lịch khác!");
                            return response;
                        }
                    }
                }
            }

            Account user = accountRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Lỗi xác thực người dùng!"));

            Booking booking = new Booking();
            booking.setAccount(user);
            booking.setTotalPrice(request.getTotalPaid());
            booking.setOriginalPrice(request.getOriginalPrice());
            booking.setDepositAmount(request.getDepositAmount());
            booking.setCheckInDate(request.getCheckInDate());
            booking.setCheckOutDate(request.getCheckOutDate());
            booking.setStatus(Booking.Status.processing);

            if (request.getPromotionId() != null) {
                Promotion promotion = promotionRepository.findById(request.getPromotionId())
                        .orElseThrow(() -> new RuntimeException("Mã khuyến mãi không hợp lệ!"));

                if (promotion.getUsedCount() >= promotion.getUsageLimit()) {
                    throw new RuntimeException("Mã khuyến mãi này đã hết lượt sử dụng!");
                }

                booking.setPromotion(promotion);
                promotion.setUsedCount(promotion.getUsedCount() + 1);
                promotionRepository.save(promotion);
            }

            Booking savedBooking = bookingRepository.save(booking);

            BookingDetail detail = new BookingDetail();
            detail.setBooking(savedBooking);
            detail.setRoom(room);
            detail.setUnitPrice(room.getDefaultPrice());

            bookingDetailRepository.save(detail);

            response.put("success", true);
            response.put("message", "Đặt phòng thành công! Mã đơn của bạn là #" + savedBooking.getBookingId());

        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
        }

        return response;
    }

    public List<BookingHistoryDto> getBookingHistory(Integer accountId) {
        List<BookingDetail> details = bookingDetailRepository.findHistoryByAccountId(accountId);

        return details.stream().map(bd -> {
            BookingHistoryDto dto = new BookingHistoryDto();
            dto.setBookingID(bd.getBooking().getBookingId());
            dto.setHotelID(bd.getRoom().getHotel().getHotelId());
            dto.setNameHotel(bd.getRoom().getHotel().getNameHotel());
            dto.setRoomType(bd.getRoom().getRoomType());
            dto.setCheckInDate(bd.getBooking().getCheckInDate());
            dto.setCheckOutDate(bd.getBooking().getCheckOutDate());
            dto.setTotalPrice(bd.getBooking().getTotalPrice());
            dto.setStatus(bd.getBooking().getStatus().name());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cancelBooking(Integer bookingId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt phòng!"));

            if (booking.getStatus() != Booking.Status.processing) {
                response.put("success", false);
                response.put("message", "Đơn hàng đã xác nhận hoặc đã hủy, không thể thao tác!");
                return response;
            }

            booking.setStatus(Booking.Status.cancelled);
            bookingRepository.save(booking);

            response.put("success", true);
            response.put("message", "Đã hủy đơn đặt phòng thành công.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    public List<BookingAdminDto> getAllBookingsForAdmin() {
        List<BookingDetail> details = bookingDetailRepository.findAllBookingsForAdmin();

        return details.stream().map(bd -> {
            BookingAdminDto dto = new BookingAdminDto();
            dto.setBookingID(bd.getBooking().getBookingId());
            dto.setUsername(bd.getBooking().getAccount().getUsername());
            dto.setHotelID(bd.getRoom().getHotel().getHotelId());
            dto.setNameHotel(bd.getRoom().getHotel().getNameHotel());
            dto.setRoomType(bd.getRoom().getRoomType());
            dto.setCheckInDate(bd.getBooking().getCheckInDate());
            dto.setCheckOutDate(bd.getBooking().getCheckOutDate());
            dto.setTotalPrice(bd.getBooking().getTotalPrice());
            dto.setStatus(bd.getBooking().getStatus().name());
            return dto;
        }).collect(Collectors.toList());
    }

    // BookingService.java — chỉ sửa method updateBookingStatus
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateBookingStatus(Integer bookingId, String newStatus) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt phòng!"));

            Booking.Status statusEnum = Booking.Status.valueOf(newStatus);
            booking.setStatus(statusEnum);
            bookingRepository.save(booking);

            // ==========================================
            // LOGIC MỚI: CHỈ TĂNG LƯỢT ĐẶT VÀ GỬI EMAIL KHI ADMIN DUYỆT (SUCCESS)
            // ==========================================
            if (statusEnum == Booking.Status.success) {
                // Lấy chi tiết đơn để dò ra khách sạn
                List<BookingDetail> details = bookingDetailRepository.findByBooking_BookingId(bookingId);
                
                if (!details.isEmpty()) {
                    Room room = details.get(0).getRoom();
                    Hotel hotel = room.getHotel();
                    
                    int currentCount = hotel.getBookingsCount() != null ? hotel.getBookingsCount() : 0;
                    hotel.setBookingsCount(currentCount + 1);
                    
                    // Lưu lại số lượt đặt mới vào Database
                    hotelRepository.save(hotel);

                    // THÊM LOGIC GỬI EMAIL TẠI ĐÂY
                    try {
                        Account account = booking.getAccount();
                        // Lưu ý: Hãy sửa lại các hàm getEmail(), getFullName(), getAddress() 
                        // cho đúng với tên thuộc tính trong Entity Account và Hotel của bạn
                        String toEmail = account.getEmail(); 
                        String customerName = account.getUsername(); // hoặc getFullName()
                        String hotelName = hotel.getNameHotel();
                        String checkIn = booking.getCheckInDate().toString();
                        String checkOut = booking.getCheckOutDate().toString();
                        String totalPaid = String.valueOf(booking.getTotalPrice());
                        String hotelAddress = hotel.getAddress(); // Nếu hotel có trường address

                        emailService.sendBookingConfirmationEmail(
                                toEmail, customerName, hotelName, checkIn, checkOut,
                                String.valueOf(booking.getBookingId()), totalPaid, hotelAddress
                        );
                    } catch (Exception e) {
                        System.err.println("Lỗi khi gửi email xác nhận: " + e.getMessage());
                        // Có thể log lỗi ra nhưng không throw để tránh rollback việc duyệt đơn
                    }
                }
            }
            // ==========================================

            response.put("success", true);
            response.put("message", "Đã cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }
    
}