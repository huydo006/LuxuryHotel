package com.luxuryhotel.luxury_hotel_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.luxuryhotel.luxury_hotel_be.entity.Account;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByUsername(String username);
    
    boolean existsByUsername(String username);

    // Thêm hàm lấy danh sách khách hàng
    List<Account> findByRole(Account.Role role);

    // Thêm hàm tìm kiếm khách hàng theo từ khóa
    @Query("SELECT a FROM Account a WHERE a.role = 'Customer' AND " +
           "(LOWER(a.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Account> searchCustomers(@Param("keyword") String keyword);
}