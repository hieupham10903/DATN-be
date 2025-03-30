package com.example.datnbe.Entity.DTO;

import lombok.*;

import java.time.LocalDate;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
    private String id;
    private String name;
    private LocalDate dob;
    private String gender;
    private String code;
    private String email;
    private String role;
}
