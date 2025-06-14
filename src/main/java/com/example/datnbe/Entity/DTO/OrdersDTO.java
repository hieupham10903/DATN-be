package com.example.datnbe.Entity.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersDTO {
    private String id;
    private String userId;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalAmount;
    private String address;

    private String paymentId;
    private LocalDateTime paymentDate;
}
