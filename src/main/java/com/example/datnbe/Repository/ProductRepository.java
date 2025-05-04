package com.example.datnbe.Repository;

import com.example.datnbe.Entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Products, String>, JpaSpecificationExecutor<Products> {
}