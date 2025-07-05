package com.example.datnbe.Service;

import com.example.datnbe.Entity.Account;
import com.example.datnbe.Entity.DTO.AccountDTO;
import com.example.datnbe.Entity.DTO.EmployeeDTO;
import com.example.datnbe.Entity.Employee;
import com.example.datnbe.Entity.Orders;
import com.example.datnbe.Entity.Request.AccountRequest;
import com.example.datnbe.Mapper.AccountMapper;
import com.example.datnbe.Mapper.EmployeeMapper;
import com.example.datnbe.Repository.AccountRepository;
import com.example.datnbe.Repository.EmployeeRepository;
import com.example.datnbe.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AccountService {
    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private OrderRepository orderRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean authenticateAccountAdmin(String username, String password) {
        Optional<Account> account = accountRepository.findByUsername(username);

        if (account.isPresent()) {
            Account acc = account.get();

            if (("EMPLOYEE".equals(acc.getType()) || "ADMIN".equals(acc.getType()))
                    && passwordEncoder.matches(password, acc.getPassword())) {
                return true;
            }
        }

        return false;
    }

    public boolean authenticateAccountClient(String username, String password) {
        Optional<Account> account = accountRepository.findByUsernameAndType(username, "GUEST");

        if (account.isPresent()) {
            return passwordEncoder.matches(password, account.get().getPassword());
        }

        return false;
    }

    public boolean registerAccount(String username, String password) throws Exception {
        if (accountRepository.findByUsername(username).isPresent()) {
            return false;
        }

        Employee newUser = new Employee();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setName(username);
        newUser.setCode("GUEST-" + UUID.randomUUID().toString());
        newUser.setRole("GUEST");

        employeeRepository.save(newUser);

        String encodedPassword = passwordEncoder.encode(password);

        Account newAccount = new Account();
        newAccount.setId(UUID.randomUUID().toString());
        newAccount.setUsername(username);
        newAccount.setPassword(encodedPassword);
        newAccount.setType("GUEST");
        newAccount.setIdEmployee(newUser.getId());

        accountRepository.save(newAccount);

        Orders orders = new Orders();
        orders.setId(UUID.randomUUID().toString());
        orders.setUserId(newUser.getId());
        orders.setOrderDate(null);
        orders.setStatus("pending");

        orderRepository.save(orders);
        return true;
    }

    public EmployeeDTO getUserInfo (String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        Employee employee = employeeRepository.findById(account.getIdEmployee())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        Orders orders = orderRepository.findByUserId(account.getIdEmployee())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

        EmployeeDTO employeeDTO = employeeMapper.toDto(employee);
        employeeDTO.setUsername(account.getUsername());
        employeeDTO.setOrderId(orders.getId());

        return employeeDTO;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<Account> optionalAccount = accountRepository.findByUsername(username);

        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();

            // Kiểm tra mật khẩu cũ
            if (passwordEncoder.matches(oldPassword, account.getPassword())) {
                // Mã hóa và lưu mật khẩu mới
                account.setPassword(passwordEncoder.encode(newPassword));
                accountRepository.save(account);
                return true;
            }
        }

        return false;
    }


}
