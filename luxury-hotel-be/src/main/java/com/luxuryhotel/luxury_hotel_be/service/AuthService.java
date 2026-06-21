package com.luxuryhotel.luxury_hotel_be.service;

import com.luxuryhotel.luxury_hotel_be.dto.AuthResponse;
import com.luxuryhotel.luxury_hotel_be.dto.LoginRequest;
import com.luxuryhotel.luxury_hotel_be.dto.RegisterRequest;
import com.luxuryhotel.luxury_hotel_be.dto.UserDto;
import com.luxuryhotel.luxury_hotel_be.entity.Account;
import com.luxuryhotel.luxury_hotel_be.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Xử lý logic Đăng nhập
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Tìm user trong Database theo username
        Optional<Account> accountOpt = accountRepository.findByUsername(request.getUsername());

        // 2. Kiểm tra tài khoản có tồn tại và mật khẩu có khớp không
        // (Lưu ý: Hiện tại đang check mật khẩu plain-text, thực tế đi làm sẽ dùng BCrypt)
        if (accountOpt.isEmpty() || !accountOpt.get().getPassword().equals(request.getPassword())) {
            return new AuthResponse(false, "Tên đăng nhập hoặc mật khẩu không chính xác!", null);
        }

        Account account = accountOpt.get();
        
        // 3. Chuyển đổi (Map) Entity Account sang UserDto để giấu mật khẩu trước khi gửi về Frontend
        UserDto userDto = new UserDto();
        userDto.setAccountId(account.getAccountId());
        userDto.setUsername(account.getUsername());
        userDto.setFullName(account.getFullName());
        userDto.setEmail(account.getEmail());
        
        // Xử lý logic vênh Role: Map từ Enum của DB sang String mà JS của bạn đang dùng
        if (account.getRole() == Account.Role.Manager) {
            userDto.setRole("admin");
        } else {
            userDto.setRole("customer");
        }

        // 4. Trả về kết quả thành công kèm thông tin User
        return new AuthResponse(true, "Đăng nhập thành công!", userDto);
    }

    /**
     * Xử lý logic Đăng ký
     */
    public AuthResponse register(RegisterRequest request) {
        // 1. Kiểm tra username đã bị người khác đăng ký chưa
        if (accountRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse(false, "Tên đăng nhập này đã tồn tại!", null);
        }

        // 2. Tạo mới một Entity Account từ dữ liệu Frontend gửi lên
        Account newAccount = new Account();
        newAccount.setFullName(request.getFullName());
        newAccount.setEmail(request.getEmail());
        newAccount.setUsername(request.getUsername());
        newAccount.setPassword(request.getPassword()); // TODO: Sau này nên mã hóa mật khẩu ở bước này
        
        // Mặc định tài khoản đăng ký mới trên web luôn là Customer (Khách hàng)
        newAccount.setRole(Account.Role.Customer); 

        // 3. Lưu xuống Database
        accountRepository.save(newAccount);

        // 4. Trả về thông báo thành công (Không cần trả về UserDto ở bước đăng ký)
        return new AuthResponse(true, "Đăng ký thành công! Vui lòng đăng nhập.", null);
    }
}