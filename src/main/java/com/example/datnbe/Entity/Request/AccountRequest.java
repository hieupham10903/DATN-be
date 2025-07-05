package com.example.datnbe.Entity.Request;

import lombok.Data;

@Data
public class AccountRequest {
    private String username;
    private String password;
    private String newPassword;
}
