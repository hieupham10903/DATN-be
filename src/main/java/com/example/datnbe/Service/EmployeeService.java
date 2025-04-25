package com.example.datnbe.Service;

import com.example.datnbe.Entity.Criteria.EmployeeCriteria;
import com.example.datnbe.Entity.DTO.EmployeeDTO;
import com.example.datnbe.Entity.Employee;
import com.example.datnbe.Entity.Employee_;
import com.example.datnbe.Mapper.EmployeeMapper;
import com.example.datnbe.Repository.EmployeeRepository;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService extends ArcQueryService<Employee> {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    public Page<EmployeeDTO> findByCriteria(EmployeeCriteria criteria, Pageable page) {
        final Specification<Employee> specification = createSpecification(criteria);
        return employeeRepository.findAll(specification, page).map(employeeMapper::toDto);
    }

    protected Specification<Employee> createSpecification(EmployeeCriteria criteria) {
        Specification<Employee> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getName() != null && !"undefined".equals(criteria.getName().getContains())) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Employee_.name));
            }
            if (criteria.getGender() != null && !"undefined".equals(criteria.getGender().getContains())) {
                specification = specification.and(buildStringSpecification(criteria.getGender(), Employee_.gender));
            }
            if (criteria.getRole() != null && !"undefined".equals(criteria.getRole().getEquals())) {
                specification = specification.and(buildStringSpecification(criteria.getRole(), Employee_.role));
            }
            if (criteria.getEmail() != null && !"undefined".equals(criteria.getEmail().getContains())) {
                specification = specification.and(buildStringSpecification(criteria.getEmail(), Employee_.email));
            }
        }
        return specification;
    }

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
        Employee employee = new Employee(UUID.randomUUID().toString(), dto.getName(), dto.getDob(), dto.getGender(),
                dto.getCode(), dto.getEmail(), dto.getRole());
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
