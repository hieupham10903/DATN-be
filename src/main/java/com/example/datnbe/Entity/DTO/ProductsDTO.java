package com.example.datnbe.Entity.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductsDTO {
    private String id;
    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;

    private String categoryId;
    private String warehouseId;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String imageDetail;
    private List<String> deletedImages;

    private String categoryName;
    private String warehouseName;
}
