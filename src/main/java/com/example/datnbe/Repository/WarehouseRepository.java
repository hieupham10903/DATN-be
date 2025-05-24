package com.example.datnbe.Repository;

import com.example.datnbe.Entity.Warehouses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouses, String>, JpaSpecificationExecutor<Warehouses> {
}