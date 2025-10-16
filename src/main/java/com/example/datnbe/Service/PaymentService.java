package com.example.datnbe.Service;

import com.example.datnbe.Entity.Criteria.PaymentCriteria;
import com.example.datnbe.Entity.DTO.OrderItemsDTO;
import com.example.datnbe.Entity.DTO.PaymentStatisticByMonthDTO;
import com.example.datnbe.Entity.DTO.PaymentsDTO;
import com.example.datnbe.Entity.DTO.PaymentsRequestDTO;
import com.example.datnbe.Entity.*;
import com.example.datnbe.Mapper.EmployeeMapper;
import com.example.datnbe.Mapper.OrderItemsMapper;
import com.example.datnbe.Mapper.OrderMapper;
import com.example.datnbe.Mapper.PaymentMapper;
import com.example.datnbe.Repository.*;
import com.example.datnbe.config.VnPayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService extends ArcQueryService<Payments> {
    private final VnPayProperties vnpayProps;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemsRepository orderItemsRepository;
    @Autowired
    private PaymentMapper paymentMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PaymentRepository paymentsRepository;
    @Autowired
    private OrderItemsMapper orderItemsMapper;
    @Autowired
    private ProductRepository productsRepository;
    @Autowired
    private OrderRepository ordersRepository;

    public void paymentSuccess(String orderId) {
        Optional<Orders> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new ServiceException("Không tìm thấy đơn hàng.");
        }

        optionalOrder.get().setTotalAmount(BigDecimal.valueOf(0));
        orderRepository.save(optionalOrder.get());

        Optional<Payments> paymentsOptional = paymentRepository.findLatestByOrderIdAndStatus(orderId, "pending");
        if (paymentsOptional.isPresent()) {
            Payments payment = paymentsOptional.get();
            payment.setStatus("paid");
            paymentRepository.save(payment);
        }

        List<OrderItems> existingItemsWithOrderTime = orderItemsRepository.findAllByOrderIdAndOrderTimeNotNull(orderId);
        int nextOrderTime = existingItemsWithOrderTime.stream()
                .mapToInt(OrderItems::getOrderTime)
                .max()
                .orElse(0) + 1;

        List<OrderItems> itemsWithoutOrderTime = orderItemsRepository.findAllByOrderIdAndOrderTimeNull(orderId);
        for (OrderItems item : itemsWithoutOrderTime) {
            item.setOrderTime(nextOrderTime);
        }

        orderItemsRepository.saveAll(itemsWithoutOrderTime);
    }

    public List<PaymentStatisticByMonthDTO> getStatisticByMonth(LocalDate start, LocalDate end) {
        List<Object[]> rawData = paymentRepository.getStatisticByMonthNative(start, end);

        return rawData.stream()
                .map(row -> new PaymentStatisticByMonthDTO((String) row[0], (BigDecimal) row[1]))
                .collect(Collectors.toList());
    }

    public List<PaymentsDTO> getAllByDateBetween(LocalDateTime start, LocalDateTime end) {
        List<Payments> payments = paymentRepository.findAllByDateBetween(start, end);

        List<String> orderIds = payments.stream()
                .map(Payments::getOrderId)
                .distinct()
                .collect(Collectors.toList());

        List<Orders> orders = orderRepository.findAllById(orderIds);

        List<String> userIds = orders.stream()
                .map(Orders::getUserId)
                .distinct()
                .collect(Collectors.toList());

        List<Employee> users = employeeRepository.findAllById(userIds);

        Map<String, String> orderIdToUserId = orders.stream()
                .collect(Collectors.toMap(Orders::getId, Orders::getUserId));

        Map<String, String> userIdToName = users.stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        List<PaymentsDTO> dtoList = paymentMapper.toDtoList(payments);
        for (PaymentsDTO dto : dtoList) {
            String userId = orderIdToUserId.get(dto.getOrderId());
            dto.setUserName(userIdToName.get(userId));
        }

        return dtoList;
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal total = paymentRepository.getTotalRevenue();
        return total != null ? total : BigDecimal.ZERO;
    }

    public Page<PaymentsDTO> findByCriteria(PaymentCriteria criteria, Pageable page) {
        final Specification<Payments> specification = createSpecification(criteria);
        return paymentRepository.findAll(specification, page).map(paymentMapper::toDto);
    }

    protected Specification<Payments> createSpecification(PaymentCriteria criteria) {
        Specification<Payments> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getOrderId() != null && !"undefined".equals(criteria.getOrderId().getContains())) {
                specification = specification.and(buildStringSpecification(criteria.getOrderId(), Payments_.orderId));
            }
            if (criteria.getPaymentDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getPaymentDate(), Payments_.paymentDate));
            }
            if (criteria.getAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmount(), Payments_.amount));
            }
            if (criteria.getMethod() != null && !"undefined".equals(criteria.getMethod().getEquals())) {
                specification = specification.and(buildStringSpecification(criteria.getMethod(), Payments_.method));
            }
            if (criteria.getStatus() != null && !"undefined".equals(criteria.getStatus().getEquals())) {
                specification = specification.and(buildStringSpecification(criteria.getStatus(), Payments_.status));
            }
        }
        return specification;
    }

    public PaymentsDTO getDetailPayment(String paymentId) throws Exception {
        Payments payments = paymentsRepository.findById(paymentId)
                .orElseThrow(() -> new ServiceException("Không tìm thấy đơn hàng"));

        PaymentsDTO dto = paymentMapper.toDto(payments);

        Orders order = ordersRepository.findById(payments.getOrderId())
                .orElseThrow(() -> new ServiceException("Không tìm thấy đơn hàng tương ứng"));

        employeeRepository.findById(order.getUserId()).ifPresent(user -> dto.setUserName(user.getName()));

        List<OrderItems> orderItems = orderItemsRepository.findAllByOrderIdAndOrderTime(
                payments.getOrderId(), payments.getOrderTime()
        );

        List<String> productIds = orderItems.stream()
                .map(OrderItems::getProductId)
                .collect(Collectors.toList());

        List<Products> products = productsRepository.findAllById(productIds);

        Map<String, String> productNameMap = products.stream()
                .collect(Collectors.toMap(Products::getId, Products::getName));

        List<OrderItemsDTO> orderItemsDTOS = orderItems.stream().map(item -> {
            OrderItemsDTO dtoItem = orderItemsMapper.toDto(item);
            dtoItem.setProductName(productNameMap.get(item.getProductId()));
            return dtoItem;
        }).collect(Collectors.toList());

        dto.setOrderItems(orderItemsDTOS);
        return dto;
    }

    public String createOrderOffline(PaymentsRequestDTO dto) {
        Optional<Orders> optionalOrder = orderRepository.findById(dto.getOrderId());
        if (optionalOrder.isEmpty()) {
            throw new ServiceException("Không tìm thấy đơn hàng.");
        }

        optionalOrder.get().setAddress(dto.getAddress());
        orderRepository.save(optionalOrder.get());

        List<OrderItems> existingItemsWithOrderTime = orderItemsRepository.findAllByOrderIdAndOrderTimeNotNull(dto.getOrderId());
        int nextOrderTime = existingItemsWithOrderTime.stream()
                .mapToInt(OrderItems::getOrderTime)
                .max()
                .orElse(0) + 1;

        Optional<Payments> paymentsOptional = paymentRepository.findLatestByOrderIdAndStatus(dto.getOrderId(), "pending");
        if (paymentsOptional.isEmpty()) {
            Payments payments = new Payments();
            payments.setId(UUID.randomUUID().toString());
            payments.setOrderId(dto.getOrderId());
            payments.setPaymentDate(LocalDateTime.now());
            payments.setAmount(optionalOrder.get().getTotalAmount());
            payments.setMethod("offline");
            payments.setStatus("paid");
            payments.setOrderTime(nextOrderTime);

            paymentRepository.save(payments);
        } else {
            paymentsOptional.get().setPaymentDate(LocalDateTime.now());
            paymentsOptional.get().setAmount(optionalOrder.get().getTotalAmount());

            paymentRepository.save(paymentsOptional.get());
        }

        List<OrderItems> itemsWithoutOrderTime = orderItemsRepository.findAllByOrderIdAndOrderTimeNull(dto.getOrderId());
        for (OrderItems item : itemsWithoutOrderTime) {
            item.setOrderTime(nextOrderTime);
        }

        orderItemsRepository.saveAll(itemsWithoutOrderTime);
        return "Thành công";
    }

}
