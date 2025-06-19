package com.example.datnbe.Entity.Criteria;

import lombok.Data;
import lombok.NoArgsConstructor;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class PaymentCriteria implements Serializable, Criteria {
    private StringFilter orderId;
    private LocalDateTimeFilter paymentDate;
    private BigDecimalFilter  amount;
    private StringFilter method;
    private StringFilter status;

    public PaymentCriteria(PaymentCriteria other) {
        this.orderId = other.orderId == null ? null : other.orderId.copy();
        this.paymentDate = other.paymentDate == null ? null : (LocalDateTimeFilter) other.paymentDate.copy();
        this.amount = other.amount == null ? null : other.amount.copy();
        this.method = other.method == null ? null : other.method.copy();
        this.status = other.status == null ? null : other.status.copy();
    }

    @Override
    public PaymentCriteria copy() {
        return new PaymentCriteria(this);
    }
}

