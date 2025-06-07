package com.example.datnbe.Service;

import com.example.datnbe.Entity.Account;
import com.example.datnbe.Entity.DTO.AccountDTO;
import com.example.datnbe.Entity.DTO.EmployeeDTO;
import com.example.datnbe.Entity.Employee;
import com.example.datnbe.Entity.Request.AccountRequest;
import com.example.datnbe.Mapper.AccountMapper;
import com.example.datnbe.Mapper.EmployeeMapper;
import com.example.datnbe.Repository.AccountRepository;
import com.example.datnbe.Repository.EmployeeRepository;
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

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean authenticateAccountAdmin(String username, String password) {
        Optional<Account> account = accountRepository.findByUsernameAndType(username, "EMPLOYEE");

        if (account.isPresent()) {
            return passwordEncoder.matches(password, account.get().getPassword());
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

        String encodedPassword = passwordEncoder.encode(password);

        Account newAccount = new Account();
        newAccount.setId(UUID.randomUUID().toString());
        newAccount.setUsername(username);
        newAccount.setPassword(encodedPassword);
        newAccount.setType("GUEST");

        accountRepository.save(newAccount);
        return true;
    }

    public EmployeeDTO getUserInfo (String username) {
        Account account = accountRepository.findByUsernameAndType(username, "GUEST")
                .orElseThrow(() -> new RuntimeException());

        Employee employee = employeeRepository.findById(account.getIdEmployee())
                .orElseThrow(() -> new RuntimeException());

        EmployeeDTO employeeDTO = employeeMapper.toDto(employee);

        return employeeDTO;
    }

}
