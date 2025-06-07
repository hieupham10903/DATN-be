package com.example.datnbe.Repository;

import com.example.datnbe.Entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, String>, JpaSpecificationExecutor<Orders> {
    Optional<Orders> findByUserId(String userId);
}