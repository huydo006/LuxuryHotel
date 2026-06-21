package com.luxuryhotel.luxury_hotel_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.luxuryhotel.luxury_hotel_be.entity.Account;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    // Tự động sinh câu lệnh SQL: SELECT * FROM accounts WHERE username = ?
    Optional<Account> findByUsername(String username);
    
    // Check xem username đã tồn tại chưa lúc đăng ký
    boolean existsByUsername(String username);
}