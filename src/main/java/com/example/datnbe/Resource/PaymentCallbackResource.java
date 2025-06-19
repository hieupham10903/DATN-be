package com.example.datnbe.Resource;


import com.example.datnbe.Entity.Criteria.PaymentCriteria;
import com.example.datnbe.Entity.Criteria.ProductCriteria;
import com.example.datnbe.Entity.DTO.PaymentStatisticByMonthDTO;
import com.example.datnbe.Entity.DTO.PaymentsDTO;
import com.example.datnbe.Entity.DTO.PaymentsRequestDTO;
import com.example.datnbe.Entity.DTO.ProductsDTO;
import com.example.datnbe.Entity.Orders;
import com.example.datnbe.Entity.Payments;
import com.example.datnbe.Repository.OrderRepository;
import com.example.datnbe.Repository.PaymentRepository;
import com.example.datnbe.Service.PaymentService;
import com.example.datnbe.Service.VNPAYService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentCallbackResource {
    private final PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private VNPAYService vnPayService;

    @PostMapping("/submitOrder")
    public ResponseEntity<String> submidOrder(@RequestBody PaymentsRequestDTO dto,
                                              HttpServletRequest request){
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = vnPayService.createOrder(request, dto, baseUrl);
        return ResponseEntity.ok(vnpayUrl);
    }

    @GetMapping("/payment-statistic-by-month")
    public ResponseEntity<List<PaymentStatisticByMonthDTO>> getStatisticByMonth(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate
    ) {
        List<PaymentStatisticByMonthDTO> result = paymentService.getStatisticByMonth(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/payment-sucess/{orderId}")
    public ResponseEntity<Void> paymentSuccess(@PathVariable String orderId) {
        paymentService.paymentSuccess(orderId);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/get-all-payment-by-date")
    public ResponseEntity<List<PaymentsDTO>> getAllByDateBetween(
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate
    ) {
        List<PaymentsDTO> result = paymentService.getAllByDateBetween(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/total-revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        BigDecimal totalRevenue = paymentService.getTotalRevenue();
        return ResponseEntity.ok(totalRevenue);
    }

    @PostMapping("/search-payment")
    public ResponseEntity<List<PaymentsDTO>> searchProduct(@RequestBody PaymentCriteria criteria, Pageable pageable) {
        Page<PaymentsDTO> page = paymentService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
