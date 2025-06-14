package com.example.datnbe.Service;

import com.example.datnbe.Entity.DTO.PaymentStatisticByMonthDTO;
import com.example.datnbe.Entity.DTO.PaymentsRequestDTO;
import com.example.datnbe.Entity.Orders;
import com.example.datnbe.Entity.Payments;
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

    public String generatePaymentUrl(PaymentsRequestDTO dto) {
        Optional<Orders> optionalOrder = orderRepository.findById(dto.getOrderId());
        if (optionalOrder.isEmpty()) {
            throw new ServiceException("Không tìm thấy đơn hàng.");
        }

        optionalOrder.get().setAddress(dto.getAddress());
        orderRepository.save(optionalOrder.get());

        Optional<Payments> paymentsOptional = paymentRepository.findByOrderId(dto.getOrderId());
        if (paymentsOptional.isEmpty()) {
            Payments payments = new Payments();
            payments.setId(UUID.randomUUID().toString());
            payments.setOrderId(dto.getOrderId());
            payments.setPaymentDate(LocalDateTime.now());
            payments.setAmount(optionalOrder.get().getTotalAmount());
            payments.setMethod("bank_transfer");
            payments.setStatus("pending");

            paymentRepository.save(payments);
        } else {
            paymentsOptional.get().setPaymentDate(LocalDateTime.now());
            paymentsOptional.get().setAmount(optionalOrder.get().getTotalAmount());

            paymentRepository.save(paymentsOptional.get());
        }

        Orders order = optionalOrder.get();
        String vnp_TxnRef = UUID.randomUUID().toString();
        String vnp_OrderInfo = "Thanh toán đơn hàng " + dto.getOrderId();
        String vnp_Amount = String.valueOf(order.getTotalAmount().longValue() * 100);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnpayProps.getTmnCode());
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayProps.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
        vnp_Params.put("vnp_CreateDate", DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));

        return buildSecureUrl(vnp_Params);
    }


    public boolean validateSignature(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (!"vnp_SecureHash".equals(entry.getKey()) && !"vnp_SecureHashType".equals(entry.getKey())) {
                fields.put(entry.getKey(), entry.getValue()[0]);
            }
        }

        List<String> sortedKeys = new ArrayList<>(fields.keySet());
        Collections.sort(sortedKeys);

        StringBuilder hashData = new StringBuilder();
        for (String key : sortedKeys) {
            String value = fields.get(key);
            if (value != null && !value.isEmpty()) {
                // Mã hóa giá trị tham số giống buildSecureUrl
                hashData.append(key).append('=').append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                if (!key.equals(sortedKeys.get(sortedKeys.size() - 1))) {
                    hashData.append('&');
                }
            }
        }

        String secureHash = hmacSHA512(vnpayProps.getHashSecret(), hashData.toString());
        String receivedHash = request.getParameter("vnp_SecureHash");
        log.debug("Calculated hash: {}, Received hash: {}", secureHash, receivedHash);
        return secureHash.equalsIgnoreCase(receivedHash);
    }

    private String buildSecureUrl(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String value = params.get(fieldName);
            if ((value != null) && (!value.isEmpty())) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
                query.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append('&');
            }
        }

        hashData.setLength(hashData.length() - 1);
        query.setLength(query.length() - 1);

        String secureHash = hmacSHA512(vnpayProps.getHashSecret(), hashData.toString());
        return vnpayProps.getUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    private String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Failed to calculate HMAC SHA512 for data: {}", data, e);
            throw new RuntimeException("Failed to calculate HMAC SHA512", e);
        }
    }

    public List<PaymentStatisticByMonthDTO> getStatisticByMonth(LocalDate start, LocalDate end) {
        List<Object[]> rawData = paymentRepository.getStatisticByMonthNative(start, end);

        return rawData.stream()
                .map(row -> new PaymentStatisticByMonthDTO((String) row[0], (BigDecimal) row[1]))
                .collect(Collectors.toList());
    }
}
