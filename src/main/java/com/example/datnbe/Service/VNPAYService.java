package com.example.datnbe.Service;

import com.example.datnbe.Entity.DTO.PaymentsRequestDTO;
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
import com.example.datnbe.config.VNPAYConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VNPAYService {
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

    public String createOrder(HttpServletRequest request, PaymentsRequestDTO dto, String urlReturn) {
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
            payments.setMethod("bank_transfer");
            payments.setStatus("pending");
            payments.setOrderTime(nextOrderTime);

            paymentRepository.save(payments);
        } else {
            paymentsOptional.get().setPaymentDate(LocalDateTime.now());
            paymentsOptional.get().setAmount(optionalOrder.get().getTotalAmount());

            paymentRepository.save(paymentsOptional.get());
        }

        Orders order = optionalOrder.get();

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = UUID.randomUUID().toString();
        String vnp_IpAddr = VNPAYConfig.getIpAddress(request);
        String vnp_TmnCode = VNPAYConfig.vnp_TmnCode;
        String orderType = "order-type";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(order.getTotalAmount().longValue() * 100));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toán đơn hàng " + dto.getOrderId());
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        vnp_Params.put("vnp_ReturnUrl", VNPAYConfig.vnp_Returnurl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String salt = VNPAYConfig.vnp_HashSecret;
        String vnp_SecureHash = VNPAYConfig.hmacSHA512(salt, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPAYConfig.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }

    public int orderReturn(HttpServletRequest request) {
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = VNPAYConfig.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

}

