package com.luxuryhotel.luxury_hotel_be.service;

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
public class AdminCustomerService {

    @Autowired
    private AccountRepository accountRepository;

    // Lấy danh sách khách hàng (có tìm kiếm)
    public List<Account> getAllCustomers(String keyword) {
        List<Account> allCustomers = accountRepository.findAll().stream()
                .filter(acc -> "Customer".equals(acc.getRole().name()))
                .collect(Collectors.toList());

        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            return allCustomers.stream()
                    .filter(c -> c.getFullName().toLowerCase().contains(lowerKeyword) || 
                                 c.getEmail().toLowerCase().contains(lowerKeyword) ||
                                 c.getUsername().toLowerCase().contains(lowerKeyword))
                    .collect(Collectors.toList());
        }
        return allCustomers;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createCustomer(CustomerRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Kiểm tra trùng username
            if (accountRepository.findByUsername(request.getUsername()).isPresent()) {
                response.put("success", false);
                response.put("message", "Tên đăng nhập đã tồn tại!");
                return response;
            }

            Account account = new Account();
            account.setFullName(request.getFullName());
            account.setEmail(request.getEmail());
            account.setUsername(request.getUsername());
            account.setPassword(request.getPassword()); // Cột trong DB là passwords
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

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateCustomer(Integer id, CustomerRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng!"));

            account.setFullName(request.getFullName());
            account.setEmail(request.getEmail());
            accountRepository.save(account);

            response.put("success", true);
            response.put("message", "Cập nhật thành công!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteCustomer(Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            accountRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Đã xóa khách hàng!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }
}