package com.example.datnbe.Service;

import com.example.datnbe.Entity.DTO.OrderHistoryResponseDTO;
import com.example.datnbe.Entity.DTO.OrderItemsDTO;
import com.example.datnbe.Entity.DTO.OrderItemsResponse;
import com.example.datnbe.Entity.DTO.OrdersDTO;
import com.example.datnbe.Entity.OrderItems;
import com.example.datnbe.Entity.Orders;
import com.example.datnbe.Entity.Payments;
import com.example.datnbe.Entity.Products;
import com.example.datnbe.Entity.Request.UpdateQuantityRequest;
import com.example.datnbe.Mapper.OrderItemsMapper;
import com.example.datnbe.Mapper.OrderMapper;
import com.example.datnbe.Repository.OrderItemsRepository;
import com.example.datnbe.Repository.OrderRepository;
import com.example.datnbe.Repository.PaymentRepository;
import com.example.datnbe.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    @Autowired
    private PaymentRepository paymentRepository;

    public List<OrderItemsResponse> getListOrder(String userId) {
        Orders orders = orderRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng của người dùng."));

        List<OrderItems> orderItemsList = orderItemsRepository.findAllByOrderIdAndOrderTimeNull(orders.getId());

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

    public void updateQuantity(UpdateQuantityRequest request) {
        OrderItems orderItem = orderItemsRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng."));

        Products product = productRepository.findById(orderItem.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm."));

        int oldQuantity = orderItem.getQuantity();
        int newQuantity = request.getQuantity();
        int difference = newQuantity - oldQuantity;

        if (difference > 0 && product.getStockQuantity() < difference) {
            throw new RuntimeException("Không đủ hàng trong kho để cập nhật số lượng.");
        }

        product.setStockQuantity(product.getStockQuantity() - difference);
        productRepository.save(product);

        orderItem.setQuantity(newQuantity);
        orderItemsRepository.save(orderItem);

        updateOrderTotalAmount(orderItem.getOrderId());
    }

    public OrderItemsDTO addProductToShoppingCard(UpdateQuantityRequest request) {
        Products product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Số lượng muốn thêm vượt quá tồn kho");
        }

        OrderItems existingItem = orderItemsRepository.findByOrderIdAndProductIdAndOrderTimeNull(request.getOrderId(), request.getProductId())
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            orderItemsRepository.save(existingItem);
        } else {
            OrderItems newItem = new OrderItems();
            newItem.setId(UUID.randomUUID().toString());
            newItem.setOrderId(request.getOrderId());
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(product.getPrice());
            orderItemsRepository.save(newItem);
        }

        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        productRepository.save(product);

        updateOrderTotalAmount(request.getOrderId());

        OrderItems resultItem = orderItemsRepository.findByOrderIdAndProductIdAndOrderTimeNull(request.getOrderId(), request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không thể tìm thấy đơn hàng sau khi thêm"));

        return orderItemsMapper.toDto(resultItem);
    }

    public void removeOrderItem(String orderItemId) {
        OrderItems orderItem = orderItemsRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục trong giỏ hàng."));

        Products product = productRepository.findById(orderItem.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm."));

        int restoredQuantity = orderItem.getQuantity();
        product.setStockQuantity(product.getStockQuantity() + restoredQuantity);
        productRepository.save(product);

        String orderId = orderItem.getOrderId();
        orderItemsRepository.delete(orderItem);

        updateOrderTotalAmount(orderId);
    }

    private void updateOrderTotalAmount(String orderId) {
        List<OrderItems> items = orderItemsRepository.findAllByOrderIdAndOrderTimeNull(orderId);
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng."));

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
    }

    public OrdersDTO getDetailOrder(String orderId) {
        Orders orders = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng của người dùng."));

        Payments payments = paymentRepository.findLatestByOrderIdAndStatus(orderId, null)
                .orElse(null);

        OrdersDTO dto = orderMapper.toDto(orders);

        if (payments != null) {
            dto.setPaymentId(payments.getId());
            dto.setPaymentDate(payments.getPaymentDate());
            if (orders.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
                dto.setTotalAmount(payments.getAmount());
            } else {
                dto.setTotalAmount(orders.getTotalAmount());
            }
        }

        return dto;
    }

    public List<OrderItemsDTO> getItemsWithMaxOrderTime(String orderId) {
        Integer maxOrderTime = orderItemsRepository.findMaxOrderTimeByOrderId(orderId);
        if (maxOrderTime == null) return new ArrayList<>();

        List<OrderItems> orderItems = orderItemsRepository.findAllByOrderIdAndOrderTime(orderId, maxOrderTime);

        return orderItems.stream().map(item -> {
            OrderItemsDTO dto = new OrderItemsDTO();
            dto.setId(item.getId());
            dto.setOrderId(item.getOrderId());
            dto.setProductId(item.getProductId());
            dto.setQuantity(item.getQuantity());
            dto.setPrice(item.getPrice());
            dto.setOrderTime(item.getOrderTime());

            Products product = productRepository.findNameById(item.getProductId());
            dto.setProductName(product.getName());

            return dto;
        }).toList();
    }

    public List<OrderHistoryResponseDTO> getOrderHistory(String orderId) {
        List<Integer> orderTimes = orderItemsRepository.findDistinctOrderTimeByOrderId(orderId);

        List<OrderHistoryResponseDTO> result = new ArrayList<>();

        for (Integer orderTime : orderTimes) {
            List<OrderItems> items = orderItemsRepository.findAllByOrderIdAndOrderTime(orderId, orderTime);

            List<OrderItemsDTO> dtoList = items.stream().map(item -> {
                OrderItemsDTO dto = new OrderItemsDTO();
                dto.setId(item.getId());
                dto.setOrderId(item.getOrderId());
                dto.setProductId(item.getProductId());
                dto.setQuantity(item.getQuantity());
                dto.setPrice(item.getPrice());
                dto.setOrderTime(item.getOrderTime());

                Products product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

                dto.setProductName(product.getName());
                dto.setProductImageUrl(product.getImageUrl());

                return dto;
            }).collect(Collectors.toList());

            Payments payment = paymentRepository.findByOrderIdAndOrderTime(orderId, orderTime);

            OrderHistoryResponseDTO response = new OrderHistoryResponseDTO();
            response.setOrderTime(orderTime);
            response.setItems(dtoList);

            if (payment != null) {
                response.setPaymentDate(payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : null);
                response.setPaymentStatus(payment.getStatus());
            }

            result.add(response);
        }

        return result;
    }
}
