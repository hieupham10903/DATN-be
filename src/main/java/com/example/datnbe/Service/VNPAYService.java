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

    public String createOrder(HttpServletRequest request, PaymentsRequestDTO dto, String urlReturn) throws UnsupportedEncodingException {
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
        String orderType = "other";

        long amount = order.getTotalAmount().longValue() * 100;  // Chuyển thành đơn vị nhỏ nhất (VND * 100)
        if (amount < 100000) {  // Kiểm tra min amount 1,000 VND
            throw new IllegalArgumentException("Số tiền không hợp lệ, tối thiểu 1,000 VND");
        }

        String vnp_TxnRef = UUID.randomUUID().toString();  // Sử dụng UUID thay vì random 8 ký tự để đảm bảo unique
        String vnp_IpAddr = VNPAYConfig.getIpAddress(request);  // Lấy IP thực tế từ request
        String vnp_TmnCode = VNPAYConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        // Loại bỏ vnp_BankCode để người dùng chọn thủ công
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", URLEncoder.encode("Thanh toan don hang: " + dto.getOrderId(), StandardCharsets.UTF_8.toString()));
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPAYConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 60);  // Tăng lên 60 phút để tránh lỗi timeout
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Log params để debug
        System.out.println("VNPAY Params: " + vnp_Params);
        System.out.println("CreateDate: " + vnp_CreateDate + ", ExpireDate: " + vnp_ExpireDate);
        System.out.println("IpAddr: " + vnp_IpAddr);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);  // Sắp xếp theo alphabet theo tài liệu
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnp_SecureHash = VNPAYConfig.hmacSHA512(VNPAYConfig.secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        return VNPAYConfig.vnp_PayUrl + "?" + query;
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

