package com.example.datnbe.Resource;

import com.example.datnbe.Entity.DTO.EmployeeDTO;
import com.example.datnbe.Entity.Request.AccountRequest;
import com.example.datnbe.Service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AccountResource {
    @Autowired
    private AccountService accountService;

    @PostMapping("/login-admin")
    public ResponseEntity<?> loginAdmin(@RequestBody AccountRequest accountRequest) {
        boolean isAuthenticated = accountService.authenticateAccountAdmin(accountRequest.getUsername(), accountRequest.getPassword());

        if (isAuthenticated) {
            return ResponseEntity.ok("Đăng nhập thành công");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tài khoản hoặc mật khẩu sai");
        }
    }

    @PostMapping("/login-client")
    public ResponseEntity<?> loginClient(@RequestBody AccountRequest accountRequest) {
        boolean isAuthenticated = accountService.authenticateAccountClient(accountRequest.getUsername(), accountRequest.getPassword());

        if (isAuthenticated) {
            return ResponseEntity.ok("Đăng nhập thành công");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tài khoản hoặc mật khẩu sai");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AccountRequest accountRequest) throws Exception {
        boolean isRegistered = accountService.registerAccount(accountRequest.getUsername(), accountRequest.getPassword());

        if (isRegistered) {
            return ResponseEntity.ok(Map.of("message", "Đăng ký thành công!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Tài khoản đã tồn tại!"));
        }
    }

    @PostMapping("/user-info")
    public ResponseEntity<?> userInfo(@RequestBody AccountRequest accountRequest) {
        EmployeeDTO employeeDTO = accountService.getUserInfo(accountRequest.getUsername());

        return ResponseEntity.ok(employeeDTO);
    }
}
