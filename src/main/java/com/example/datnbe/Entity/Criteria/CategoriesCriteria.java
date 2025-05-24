package com.example.datnbe.Entity.Criteria;

import lombok.Data;
import lombok.NoArgsConstructor;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.StringFilter;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class CategoriesCriteria implements Serializable, Criteria {
    private StringFilter name;

    public CategoriesCriteria(CategoriesCriteria other) {
        this.name = other.name == null ? null : other.name.copy();
    }

    @Override
    public CategoriesCriteria copy() {
        return new CategoriesCriteria(this);
    }
}
