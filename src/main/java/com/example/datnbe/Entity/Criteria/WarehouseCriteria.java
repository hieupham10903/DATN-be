package com.example.datnbe.Entity.Criteria;

import lombok.Data;
import lombok.NoArgsConstructor;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.StringFilter;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class WarehouseCriteria implements Serializable, Criteria {
    private StringFilter name;
    private StringFilter code;

    public WarehouseCriteria(WarehouseCriteria other) {
        this.name = other.name == null ? null : other.name.copy();
        this.code = other.code == null ? null : other.code.copy();
    }

    @Override
    public WarehouseCriteria copy() {
        return new WarehouseCriteria(this);
    }
}
