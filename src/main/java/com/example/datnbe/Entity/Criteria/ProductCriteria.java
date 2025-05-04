package com.example.datnbe.Entity.Criteria;

import lombok.Data;
import lombok.NoArgsConstructor;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BigDecimalFilter;
import tech.jhipster.service.filter.StringFilter;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ProductCriteria implements Serializable, Criteria {
    private StringFilter name;
    private StringFilter code;
    private BigDecimalFilter price;
    private StringFilter categoryId;
    private StringFilter shelfId;

    public ProductCriteria(ProductCriteria other) {
        this.name = other.name == null ? null : other.name.copy();
        this.code = other.code == null ? null : other.code.copy();
        this.price = other.price == null ? null : other.price.copy();
        this.categoryId = other.categoryId == null ? null : other.categoryId.copy();
        this.shelfId = other.shelfId == null ? null : other.shelfId.copy();
    }

    @Override
    public ProductCriteria copy() {
        return new ProductCriteria(this);
    }
}
