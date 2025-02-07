package com.example.datnbe.Resource;

import com.example.datnbe.Entity.Criteria.EmployeeCriteria;
import com.example.datnbe.Entity.DTO.EmployeeDTO;
import com.example.datnbe.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeResource {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/search-employee")
    public ResponseEntity<List<EmployeeDTO>> getAllCoreApi(@RequestBody EmployeeCriteria criteria, Pageable pageable) {
        Page<EmployeeDTO> page = employeeService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/get-all-employee")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployee () {
        return ResponseEntity.ok(employeeService.getAllEmployee());
    }

    @PostMapping("/get-employee-by-id")
    public ResponseEntity<EmployeeDTO> getAllEmployee (@RequestParam String id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
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
