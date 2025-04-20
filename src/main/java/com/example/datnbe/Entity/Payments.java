package com.example.datnbe.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payments")
public class Payments {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "method")
    private String method;

    @Column(name = "status")
    private String status;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getPaymentDate() {
        return this.paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
