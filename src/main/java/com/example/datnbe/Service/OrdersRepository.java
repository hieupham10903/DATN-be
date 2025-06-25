package com.example.datnbe.Service;

import com.example.datnbe.Entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, String> {
}