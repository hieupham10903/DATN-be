package com.example.datnbe.Entity.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderHistoryResponseDTO {
    private Integer orderTime;
    private String paymentStatus;
    private String paymentDate;
    private List<OrderItemsDTO> items;
}
