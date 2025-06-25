package com.example.datnbe.Service;

import com.example.datnbe.Entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository<Products, String> {
}