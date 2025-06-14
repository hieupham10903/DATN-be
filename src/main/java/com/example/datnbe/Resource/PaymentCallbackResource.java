package com.example.datnbe.Resource;


import com.example.datnbe.Entity.DTO.PaymentStatisticByMonthDTO;
import com.example.datnbe.Entity.DTO.PaymentsRequestDTO;
import com.example.datnbe.Entity.Orders;
import com.example.datnbe.Entity.Payments;
import com.example.datnbe.Repository.OrderRepository;
import com.example.datnbe.Repository.PaymentRepository;
import com.example.datnbe.Service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentCallbackResource {
    private final PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping
    public String create(@RequestBody PaymentsRequestDTO dto) {
        return paymentService.generatePaymentUrl(dto);
    }

    @GetMapping("/vnpay/callback")
    public ResponseEntity<?> vnPayCallback(HttpServletRequest request) {
        if (!paymentService.validateSignature(request)) {
            return ResponseEntity.status(302)
                    .header("Location", "https://your-frontend.com/payment-return?success=false&message=Invalid signature")
                    .build();
        }

        String bookingCode = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String amountStr = request.getParameter("vnp_Amount");

        Optional<Orders> optionalOrder = orderRepository.findById(bookingCode);
        if (optionalOrder.isEmpty()) {
            return ResponseEntity.badRequest().body("Order not found");
        }

        Orders order = optionalOrder.get();

        Payments payment = new Payments();
        payment.setId(UUID.randomUUID().toString());
        payment.setOrderId(order.getId());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)));
        payment.setMethod("VNPAY");

        String message;
        if ("00".equals(responseCode)) {
            payment.setStatus("SUCCESS");
            message = "success";
        } else {
            payment.setStatus("FAILED");
            message = "false";
        }

        paymentRepository.save(payment);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/return")
    public void handleVnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean isValid = paymentService.validateSignature(request);
        String transactionStatus = request.getParameter("vnp_TransactionStatus");
        String rawOrderInfo = request.getParameter("vnp_OrderInfo");
        String orderId = rawOrderInfo.replace("Thanh toán đơn hàng ", "");

        if (isValid && "00".equals(transactionStatus)) {
            // Cập nhật đơn hàng
            Orders order = orderRepository.findById(orderId).orElseThrow();
            order.setStatus("PAID");
            orderRepository.save(order);

            response.sendRedirect("http://localhost:3001/payment-success");
        } else {
            response.sendRedirect("http://localhost:3001/payment-fail");
        }
    }


    @GetMapping("/payment-statistic-by-month")
    public ResponseEntity<List<PaymentStatisticByMonthDTO>> getStatisticByMonth(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate
    ) {
        List<PaymentStatisticByMonthDTO> result = paymentService.getStatisticByMonth(startDate, endDate);
        return ResponseEntity.ok(result);
    }

}
