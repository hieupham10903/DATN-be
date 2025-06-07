package com.example.datnbe.Entity.Request;

import lombok.Data;

@Data
public class UpdateQuantityRequest {
    private String id;
    private Integer quantity;
}
