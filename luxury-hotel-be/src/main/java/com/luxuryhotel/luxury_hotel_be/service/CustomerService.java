package com.luxuryhotel.luxury_hotel_be.service;

import com.luxuryhotel.luxury_hotel_be.dto.CustomerDto;
import com.luxuryhotel.luxury_hotel_be.dto.CustomerRequest;
import com.luxuryhotel.luxury_hotel_be.entity.Account;
import com.luxuryhotel.luxury_hotel_be.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private AccountRepository accountRepository;

    // Lấy danh sách hoặc tìm kiếm
    public List<CustomerDto> getCustomers(String keyword) {
        List<Account> accounts;
        if (keyword != null && !keyword.isEmpty()) {
            accounts = accountRepository.searchCustomers(keyword);
        } else {
            accounts = accountRepository.findByRole(Account.Role.Customer);
        }

        return accounts.stream().map(acc -> {
            CustomerDto dto = new CustomerDto();
            dto.setAccountID(acc.getAccountId());
            dto.setUsername(acc.getUsername());
            dto.setFullName(acc.getFullName());
            dto.setEmail(acc.getEmail());
            dto.setPhoneNumber(acc.getPhoneNumber()); 
            
            return dto;
        }).collect(Collectors.toList());
    }

    // Thêm khách hàng
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createCustomer(CustomerRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (accountRepository.existsByUsername(request.getUsername())) {
                response.put("success", false);
                response.put("message", "Tên đăng nhập đã tồn tại!");
                return response;
            }

            Account account = new Account();
            account.setFullName(request.getFullName());
            account.setEmail(request.getEmail());
            
            // ==========================================
            // THÊM SỐ ĐIỆN THOẠI KHI TẠO MỚI
            // ==========================================
            account.setPhoneNumber(request.getPhoneNumber());
            
            account.setUsername(request.getUsername());
            account.setPassword(request.getPassword());
            account.setRole(Account.Role.Customer);

            accountRepository.save(account);

            response.put("success", true);
            response.put("message", "Thêm khách hàng thành công!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    // Sửa thông tin
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateCustomer(Integer id, CustomerRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng!"));

            account.setFullName(request.getFullName());
            account.setEmail(request.getEmail());
            
            // ==========================================
            // CẬP NHẬT SỐ ĐIỆN THOẠI KHI SỬA
            // ==========================================
            account.setPhoneNumber(request.getPhoneNumber());
            
            accountRepository.save(account);

            response.put("success", true);
            response.put("message", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    // Xóa khách hàng
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteCustomer(Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!accountRepository.existsById(id)) {
                response.put("success", false);
                response.put("message", "Không tìm thấy khách hàng để xóa!");
                return response;
            }

            accountRepository.deleteById(id);

            response.put("success", true);
            response.put("message", "Xóa khách hàng thành công!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }
}