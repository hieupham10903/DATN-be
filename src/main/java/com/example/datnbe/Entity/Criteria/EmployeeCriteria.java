package com.example.datnbe.Entity.Criteria;

import lombok.Data;
import lombok.NoArgsConstructor;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.StringFilter;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class EmployeeCriteria implements Serializable, Criteria {
    private StringFilter name;
    private StringFilter gender;
    private StringFilter role;
    private StringFilter email;

    public EmployeeCriteria(EmployeeCriteria other) {
        this.name = other.name == null ? null : other.name.copy();
        this.gender = other.gender == null ? null : other.gender.copy();
        this.role = other.role == null ? null : other.role.copy();
        this.email = other.email == null ? null : other.email.copy();
    }

    @Override
    public EmployeeCriteria copy() {
        return new EmployeeCriteria(this);
    }
}
