package com.luxuryhotel.luxury_hotel_be.controller;

import com.luxuryhotel.luxury_hotel_be.dto.CustomerRequest;
import com.luxuryhotel.luxury_hotel_be.entity.Account;
import com.luxuryhotel.luxury_hotel_be.service.AdminCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/customers")
@CrossOrigin(origins = "*")
public class AdminCustomerController {

    @Autowired
    private AdminCustomerService customerService;

    @GetMapping
    public ResponseEntity<List<Account>> getCustomers(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(customerService.getAllCustomers(keyword));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCustomer(@RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCustomer(@PathVariable Integer id, @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.deleteCustomer(id));
    }
}