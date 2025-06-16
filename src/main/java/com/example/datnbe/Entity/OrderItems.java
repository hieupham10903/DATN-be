package com.example.datnbe.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
public class OrderItems {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "order_time")
    private Integer orderTime;
}
