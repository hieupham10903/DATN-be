package com.example.datnbe.Service;

import com.example.datnbe.Entity.DTO.PaymentStatisticByMonthDTO;
import com.example.datnbe.Entity.DTO.PaymentsDTO;
import com.example.datnbe.Entity.DTO.PaymentsRequestDTO;
import com.example.datnbe.Entity.Employee;
import com.example.datnbe.Entity.OrderItems;
import com.example.datnbe.Entity.Orders;
import com.example.datnbe.Entity.Payments;
import com.example.datnbe.Mapper.EmployeeMapper;
import com.example.datnbe.Mapper.OrderMapper;
import com.example.datnbe.Mapper.PaymentMapper;
import com.example.datnbe.Repository.EmployeeRepository;
import com.example.datnbe.Repository.OrderItemsRepository;
import com.example.datnbe.Repository.OrderRepository;
import com.example.datnbe.Repository.PaymentRepository;
import com.example.datnbe.config.VnPayProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
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

}
