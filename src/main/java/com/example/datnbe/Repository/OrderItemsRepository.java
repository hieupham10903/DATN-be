package com.example.datnbe.Repository;

import com.example.datnbe.Entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, String>, JpaSpecificationExecutor<OrderItems> {
    @Query(value = "Select oi from OrderItems oi where oi.orderId = :orderId and oi.orderTime is null")
    List<OrderItems> findAllByOrderIdAndOrderTimeNull(String orderId);

    @Query("SELECT oi FROM OrderItems oi WHERE oi.orderId = :orderId AND oi.orderTime IS NOT NULL")
    List<OrderItems> findAllByOrderIdAndOrderTimeNotNull(String orderId);

    List<OrderItems> findAllByOrderId(String orderId);

    Optional<OrderItems> findByOrderIdAndProductIdAndOrderTimeNull(String orderId, String productId);

    @Query("SELECT MAX(oi.orderTime) FROM OrderItems oi WHERE oi.orderId = :orderId AND oi.orderTime IS NOT NULL")
    Integer findMaxOrderTimeByOrderId(String orderId);

    @Query("SELECT oi FROM OrderItems oi WHERE oi.orderId = :orderId AND oi.orderTime = :orderTime")
    List<OrderItems> findAllByOrderIdAndOrderTime(String orderId, Integer orderTime);
}