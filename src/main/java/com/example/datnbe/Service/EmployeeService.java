package com.example.datnbe.Service;

import com.example.datnbe.Entity.DTO.EmployeeDTO;
import com.example.datnbe.Entity.Employee;
import com.example.datnbe.Mapper.EmployeeMapper;
import com.example.datnbe.Repository.EmployeeRepository;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    public List<EmployeeDTO> getAllEmployee() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(String id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isEmpty()) {
            throw new ServiceException("Không tìm thấy nhân viên");
        }
        return employeeMapper.toDto(employee.get());
    }

    public EmployeeDTO createEmployee (EmployeeDTO dto) {
        Employee employee = new Employee(UUID.randomUUID().toString(), dto.getName(), dto.getDob(), dto.getGender());
        EmployeeDTO employeeDTO = employeeMapper.toDto(employeeRepository.save(employee));
        return employeeDTO;
    }

    public EmployeeDTO updateEmployee (EmployeeDTO dto) {
        Optional<Employee> employee = employeeRepository.findById(dto.getId());
        if (employee.isEmpty()) {
            throw new ServiceException("Không tìm thấy nhân viên");
        }
        assert employee != null;
        employee.get().setName(dto.getName());
        employee.get().setDob(dto.getDob());
        employee.get().setGender(dto.getGender());
        EmployeeDTO employeeDTO = employeeMapper.toDto(employeeRepository.save(employee.get()));
        return employeeDTO;
    }
}
