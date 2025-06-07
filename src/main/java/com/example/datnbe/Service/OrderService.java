package com.example.datnbe.Service;

import com.example.datnbe.Entity.DTO.OrderItemsResponse;
import com.example.datnbe.Entity.OrderItems;
import com.example.datnbe.Entity.Orders;
import com.example.datnbe.Entity.Products;
import com.example.datnbe.Entity.Request.UpdateQuantityRequest;
import com.example.datnbe.Mapper.OrderItemsMapper;
import com.example.datnbe.Mapper.OrderMapper;
import com.example.datnbe.Repository.OrderItemsRepository;
import com.example.datnbe.Repository.OrderRepository;
import com.example.datnbe.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private ProductRepository productRepository;

    public List<OrderItemsResponse> getListOrder(String userId) {
        Orders orders = orderRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng của người dùng."));

        List<OrderItems> orderItemsList = orderItemsRepository.findAllByOrderId(orders.getId());

        return orderItemsList.stream().map(item -> {
            Products product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            OrderItemsResponse response = new OrderItemsResponse();
            response.setId(item.getId());
            response.setOrderId(item.getOrderId());
            response.setProductId(item.getProductId());
            response.setQuantity(item.getQuantity());
            response.setPrice(item.getPrice());

            response.setCode(product.getCode());
            response.setName(product.getName());
            response.setCategoryId(product.getCategoryId());
            response.setWarehouseId(product.getWarehouseId());
            response.setImageUrl(product.getImageUrl());
            response.setStockQuantity(product.getStockQuantity());

            return response;
        }).collect(Collectors.toList());
    }

    public void updateQuantity(UpdateQuantityRequest updateQuantityRequest) {
        OrderItems orderItems = orderItemsRepository.findById(updateQuantityRequest.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng của người dùng."));

        Products product = productRepository.findById(orderItems.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (product.getStockQuantity() < updateQuantityRequest.getQuantity()) {
            throw new RuntimeException("Quá số lượng còn lại trong kho");
        }

        orderItems.setQuantity(updateQuantityRequest.getQuantity());
    }
}
