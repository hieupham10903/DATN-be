package com.example.datnbe.Resource;


import com.example.datnbe.Entity.DTO.PaymentStatisticByMonthDTO;
import com.example.datnbe.Service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentCallbackResource {
    private final PaymentService paymentService;

    @PostMapping
    public String create(){
       return paymentService.generatePaymentUrl();
    }
    @GetMapping("/vnpay/callback")
    public ResponseEntity<?> vnPayCallback(HttpServletRequest request) {
        if (!paymentService.validateSignature(request)) {
            // Redirect về FE với lỗi
            return ResponseEntity.status(302)
                    .header("Location", "https://your-frontend.com/payment-return?success=false&message=Invalid signature")
                    .build();
        }

        String bookingCode = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String message = null;
        if(responseCode.equals("00")){
            message = "sucess";
        }else {
            message="false";
        }

        return   ResponseEntity.ok(message);
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
