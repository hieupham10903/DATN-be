package com.example.datnbe.Service;

import com.example.datnbe.Entity.Account;
import com.example.datnbe.Entity.Criteria.EmployeeCriteria;
import com.example.datnbe.Entity.DTO.AccountDTO;
import com.example.datnbe.Entity.DTO.EmployeeDTO;
import com.example.datnbe.Entity.Employee;
import com.example.datnbe.Entity.Employee_;
import com.example.datnbe.Mapper.AccountMapper;
import com.example.datnbe.Mapper.EmployeeMapper;
import com.example.datnbe.Repository.AccountRepository;
import com.example.datnbe.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
            throw new RuntimeException("Không tìm thấy nhân viên");
        }
        Optional<Account> account = accountRepository.findByIdEmployee(id);
        if (account.isEmpty()) {
            throw new RuntimeException("Không tìm thấy tài khoản");
        }
        AccountDTO accountDTO = accountMapper.toDto(accountRepository.save(account.get()));
        EmployeeDTO employeeDTO = employeeMapper.toDto(employee.get());
        employeeDTO.setUsername(accountDTO.getUsername());
        return employeeDTO;
    }

    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        if (employeeRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã nhân viên đã tồn tại");
        }

        if (accountRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        Employee employee = new Employee(UUID.randomUUID().toString(), dto.getName(), dto.getDob(), dto.getGender(),
                dto.getCode(), dto.getEmail(), dto.getRole());
        EmployeeDTO employeeDTO = employeeMapper.toDto(employeeRepository.save(employee));

        String encodedPassword = passwordEncoder.encode("Abc@123");

        Account account = new Account(UUID.randomUUID().toString(), employee.getId(), dto.getUsername(), encodedPassword, dto.getRole());
        AccountDTO accountDTO = accountMapper.toDto(accountRepository.save(account));
        employeeDTO.setUsername(accountDTO.getUsername());

        return employeeDTO;
    }

    public EmployeeDTO updateEmployee(EmployeeDTO dto) {
        Optional<Employee> employeeOpt = employeeRepository.findById(dto.getId());
        if (employeeOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy nhân viên");
        }

        Optional<Employee> otherEmployeeWithSameCode = employeeRepository.findByCode(dto.getCode());
        if (otherEmployeeWithSameCode.isPresent() && !otherEmployeeWithSameCode.get().getId().equals(dto.getId())) {
            throw new RuntimeException("Mã nhân viên đã tồn tại");
        }

        Employee employee = employeeOpt.get();
        employee.setName(dto.getName());
        employee.setDob(dto.getDob());
        employee.setGender(dto.getGender());
        employee.setRole(dto.getRole());
        employee.setCode(dto.getCode());
        EmployeeDTO employeeDTO = employeeMapper.toDto(employeeRepository.save(employee));

        Optional<Account> accountOpt = accountRepository.findByIdEmployee(dto.getId());
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy tài khoản");
        }

        Account account = accountOpt.get();

        Optional<Account> otherAccountWithSameUsername = accountRepository.findByUsername(dto.getUsername());
        if (otherAccountWithSameUsername.isPresent() && !otherAccountWithSameUsername.get().getId().equals(account.getId())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        account.setUsername(dto.getUsername());
        account.setType(dto.getRole());
        AccountDTO accountDTO = accountMapper.toDto(accountRepository.save(account));

        employeeDTO.setUsername(accountDTO.getUsername());
        return employeeDTO;
    }

    public void deleteEmployee (String id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isEmpty()) {
            throw new RuntimeException("Không tìm thấy nhân viên");
        }
        Optional<Account> account = accountRepository.findByIdEmployee(id);
        if (account.isEmpty()) {
            throw new RuntimeException("Không tìm thấy tài khoản");
        }
        accountRepository.delete(account.get());
        employeeRepository.delete(employee.get());
    }
}
