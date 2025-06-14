package com.example.datnbe.Repository;

import com.example.datnbe.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,String>, JpaSpecificationExecutor<Employee> {
    boolean existsByCode(String code);
    Optional<Employee> findByCode(String code);
}
