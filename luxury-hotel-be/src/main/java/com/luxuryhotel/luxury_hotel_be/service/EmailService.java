package com.luxuryhotel.luxury_hotel_be.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Cập nhật thêm các tham số: bookingId, totalPaid, hotelAddress
    public void sendBookingConfirmationEmail(String toEmail, String customerName, String hotelName, 
                                             String checkIn, String checkOut, String bookingId, 
                                             String totalPaid, String hotelAddress) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // Tham số 'true' cho phép định dạng HTML
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("email_cua_em@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đặt phòng thành công - Đơn #" + bookingId);

            // Nội dung email dạng HTML để hiển thị đẹp hơn
            String content = "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>"
                    + "<h2 style='color: #0f172a;'>Xin chào " + customerName + ",</h2>"
                    + "<p>Chúc mừng bạn! Đơn đặt phòng của bạn tại <strong>" + hotelName + "</strong> đã được xác nhận thành công.</p>"
                    + "<div style='background: #f8fafc; padding: 15px; border-radius: 8px; border: 1px solid #e2e8f0;'>"
                    + "<h3>📋 Thông tin đơn hàng:</h3>"
                    + "<p><strong>Mã đơn hàng:</strong> #" + bookingId + "</p>"
                    + "<p><strong>Địa chỉ khách sạn:</strong> " + hotelAddress + "</p>"
                    + "<p><strong>Ngày nhận phòng:</strong> " + checkIn + "</p>"
                    + "<p><strong>Ngày trả phòng:</strong> " + checkOut + "</p>"
                    + "<p style='font-size: 1.2rem; color: #dc2626;'><strong>Tổng thanh toán:</strong> " + totalPaid + " VNĐ</p>"
                    + "</div>"
                    + "<p>Cảm ơn bạn đã tin tưởng dịch vụ của Luxury Hotel. Chúc bạn một kỳ nghỉ tuyệt vời!</p>"
                    + "<br><p>Trân trọng,<br><b>Ban Quản lý Luxury Hotel</b></p>"
                    + "</body></html>";

            helper.setText(content, true); // true để gửi dưới dạng HTML
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            // Có thể thêm log lỗi hoặc xử lý ngoại lệ tùy dự án
        }
    }
}