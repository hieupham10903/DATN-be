package com.example.datnbe.Entity.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaymentsDTO {
    private String id;
    private String orderId;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private String method;
    private String status;
    private Integer orderTime;

    private String userName;
    private List<OrderItemsDTO> orderItems;
}
