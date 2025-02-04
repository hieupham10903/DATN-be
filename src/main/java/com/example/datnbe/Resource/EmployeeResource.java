package com.example.datnbe.Resource;

import com.example.datnbe.Entity.DTO.EmployeeDTO;
import com.example.datnbe.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeResource {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/get-all-employee")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployee () {
        return ResponseEntity.ok(employeeService.getAllEmployee());
    }

    @PostMapping("/get-employee-by-id")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployee (@RequestParam String id) {
        return ResponseEntity.ok(employeeService.getAllEmployee());
    }

    @PostMapping("/create-employee")
    public ResponseEntity<EmployeeDTO> createEmployee (@RequestBody EmployeeDTO dto) {
        return ResponseEntity.ok(employeeService.createEmployee(dto));
    }

    @PostMapping("/update-employee")
    public ResponseEntity<EmployeeDTO> updateEmployee (@RequestBody EmployeeDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(dto));
    }
}
