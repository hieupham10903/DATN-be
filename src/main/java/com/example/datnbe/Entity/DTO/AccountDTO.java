package com.example.datnbe.Entity.DTO;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {
    private String id;
    private String idEmployee;
    private String username;
    private String password;
    private String type;
}
