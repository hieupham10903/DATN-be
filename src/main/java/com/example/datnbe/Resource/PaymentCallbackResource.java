package com.example.datnbe.Resource;


import com.example.datnbe.Service.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentCallbackResource {
    private final VnpayService vnpayService;

    @PostMapping
    public String create(){
       return vnpayService.generatePaymentUrl();
    }
    @GetMapping("/vnpay/callback")
    public ResponseEntity<?> vnPayCallback(HttpServletRequest request) {
        if (!vnpayService.validateSignature(request)) {
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

}
