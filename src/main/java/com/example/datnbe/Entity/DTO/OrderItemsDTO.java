package com.example.datnbe.Entity.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemsDTO {
    private String id;
    private String orderId;
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private Integer orderTime;

    private String productName;
    private String productImageUrl;
}
