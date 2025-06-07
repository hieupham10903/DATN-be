package com.example.datnbe.Repository;

import com.example.datnbe.Entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, String>, JpaSpecificationExecutor<OrderItems> {
    List<OrderItems> findAllByOrderId(String orderId);
}