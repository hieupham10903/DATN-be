package com.example.datnbe.Entity.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentsDTO {
    private String id;
    private String orderId;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private String method;
    private String status;

    private String userName;
}
