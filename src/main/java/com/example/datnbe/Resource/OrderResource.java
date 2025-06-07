package com.example.datnbe.Resource;

import com.example.datnbe.Entity.DTO.OrderItemsResponse;
import com.example.datnbe.Entity.Request.UpdateQuantityRequest;
import com.example.datnbe.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderResource {

    @Autowired
    private OrderService orderService;

    @GetMapping("/list-all-order-item/{userId}")
    public ResponseEntity<List<OrderItemsResponse>> ListAllOrderItem(@PathVariable String userId) {
        List<OrderItemsResponse> list = orderService.getListOrder(userId);
        return ResponseEntity.ok().body(list);
    }

    @PostMapping("/update-quantity")
    public ResponseEntity<Void> ListAllOrderItem(@RequestBody UpdateQuantityRequest updateQuantityRequest) {
        orderService.updateQuantity(updateQuantityRequest);
        return ResponseEntity.ok().body(null);
    }
}
