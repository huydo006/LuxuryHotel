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
import com.luxuryhotel.luxury_hotel_be.entity.Promotion;
import com.luxuryhotel.luxury_hotel_be.entity.Room;
import com.luxuryhotel.luxury_hotel_be.repository.AccountRepository;
import com.luxuryhotel.luxury_hotel_be.repository.BookingDetailRepository;
import com.luxuryhotel.luxury_hotel_be.repository.BookingRepository;
import com.luxuryhotel.luxury_hotel_be.repository.PromotionRepository;
import com.luxuryhotel.luxury_hotel_be.repository.RoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // Tự động tạo Constructor cho các biến 'final', code sạch hơn @Autowired
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final RoomRepository roomRepository;
    private final AccountRepository accountRepository;
    private final PromotionRepository promotionRepository; // Thêm Repo Khuyến mãi

    // BẮT BUỘC DÙNG rollbackFor = Exception.class ĐỂ ĐẢM BẢO TOÀN VẸN DỮ LIỆU
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createBooking(BookingRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. CHỐNG RACE CONDITION: Kiểm tra lại xem phòng còn trống không
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Phòng không tồn tại!"));

            // Tái sử dụng thuật toán đếm theo từng ngày (Giống hàm sinh lịch Flatpickr)
            List<Object[]> activeBookings = bookingRepository.findActiveBookingsDatesByRoomId(request.getRoomId());
            Map<LocalDate, Integer> dailyOverlapCounts = new HashMap<>();

            for (Object[] dates : activeBookings) {
                LocalDate existingCheckIn = (LocalDate) dates[0];
                LocalDate existingCheckOut = (LocalDate) dates[1];

                // Nếu đơn hàng cũ có giao cắt với khoảng thời gian khách đang muốn đặt
                if (existingCheckIn.isBefore(request.getCheckOutDate()) && existingCheckOut.isAfter(request.getCheckInDate())) {
                    
                    // Lấy ra chính xác khoảng thời gian bị giao cắt (phần chung)
                    LocalDate startOverlap = existingCheckIn.isAfter(request.getCheckInDate()) ? existingCheckIn : request.getCheckInDate();
                    LocalDate endOverlap = existingCheckOut.isBefore(request.getCheckOutDate()) ? existingCheckOut : request.getCheckOutDate();

                    // Tăng biến đếm cho TỪNG NGÀY bị trùng trong khoảng giao cắt
                    for (LocalDate date = startOverlap; date.isBefore(endOverlap); date = date.plusDays(1)) {
                        dailyOverlapCounts.put(date, dailyOverlapCounts.getOrDefault(date, 0) + 1);
                        
                        // Nếu có BẤT KỲ NGÀY NÀO vượt quá số lượng phòng thực tế -> Chặn ngay lập tức!
                        if (dailyOverlapCounts.get(date) >= room.getQuantity()) {
                            response.put("success", false);
                            response.put("message", "Rất tiếc, ngày " + date + " đã hết phòng. Vui lòng chọn lịch khác!");
                            return response;
                        }
                    }
                }
            }

            // 2. TẠO ĐƠN ĐẶT PHÒNG (Bảng bookings)
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

            // --- XỬ LÝ PROMOTION ĐÃ HOÀN THIỆN ---
            if (request.getPromotionId() != null) {
                Promotion promotion = promotionRepository.findById(request.getPromotionId())
                        .orElseThrow(() -> new RuntimeException("Mã khuyến mãi không hợp lệ!"));

                // Kiểm tra xem mã đã hết lượt dùng chưa
                if (promotion.getUsedCount() >= promotion.getUsageLimit()) {
                    throw new RuntimeException("Mã khuyến mãi này đã hết lượt sử dụng!");
                }

                booking.setPromotion(promotion);

                // Tăng biến đếm số lần sử dụng mã và lưu lại
                promotion.setUsedCount(promotion.getUsedCount() + 1);
                promotionRepository.save(promotion);
            }

            // Lưu Booking để lấy ID sinh tự động
            Booking savedBooking = bookingRepository.save(booking);

            // 3. TẠO CHI TIẾT ĐẶT PHÒNG (Bảng booking_details)
            BookingDetail detail = new BookingDetail();
            detail.setBooking(savedBooking);
            detail.setRoom(room);
            detail.setUnitPrice(room.getDefaultPrice());

            bookingDetailRepository.save(detail);

            response.put("success", true);
            response.put("message", "Đặt phòng thành công! Mã đơn của bạn là #" + savedBooking.getBookingId());

        } catch (Exception e) {
            // Lệnh BẮT BUỘC PHẢI CÓ để báo cho Spring biết phải Rollback dù đã bị try-catch nuốt lỗi
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
        }

        return response;
    }

    // 1. Hàm lấy Lịch sử đặt phòng
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

    // 2. Hàm xử lý Hủy Đơn
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cancelBooking(Integer bookingId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt phòng!"));

            // Chỉ cho phép hủy nếu đơn đang ở trạng thái 'processing'
            if (booking.getStatus() != Booking.Status.processing) {
                response.put("success", false);
                response.put("message", "Đơn hàng đã xác nhận hoặc đã hủy, không thể thao tác!");
                return response;
            }

            // Đổi trạng thái thành cancelled
            booking.setStatus(Booking.Status.cancelled);
            bookingRepository.save(booking);

            response.put("success", true);
            response.put("message", "Đã hủy đơn đặt phòng thành công. Tiền cọc sẽ được hoàn lại trong 24h.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    // Lấy tất cả đơn cho Admin
    public List<BookingAdminDto> getAllBookingsForAdmin() {
        List<BookingDetail> details = bookingDetailRepository.findAllBookingsForAdmin();
        
        return details.stream().map(bd -> {
            BookingAdminDto dto = new BookingAdminDto();
            dto.setBookingID(bd.getBooking().getBookingId());
            dto.setUsername(bd.getBooking().getAccount().getUsername()); // Map username
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

    // Đổi trạng thái đơn hàng (Duyệt / Hủy)
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateBookingStatus(Integer bookingId, String newStatus) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt phòng!"));
            
            // Chuyển string status từ JS sang Enum
            Booking.Status statusEnum = Booking.Status.valueOf(newStatus);
            booking.setStatus(statusEnum);
            
            bookingRepository.save(booking);

            response.put("success", true);
            response.put("message", "Đã cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }
}