package com.example.datnbe.Entity.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentStatisticByMonthDTO {
    private String month;
    private BigDecimal totalAmount;

    public PaymentStatisticByMonthDTO(String month, BigDecimal totalAmount) {
        this.month = month;
        this.totalAmount = totalAmount;
    }

    public String getMonth() {
        return month;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}

