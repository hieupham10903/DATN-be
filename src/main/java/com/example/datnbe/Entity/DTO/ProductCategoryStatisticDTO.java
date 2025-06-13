package com.example.datnbe.Entity.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCategoryStatisticDTO {
    private String categoryId;
    private String categoryName;
    private Long quantity;
}
