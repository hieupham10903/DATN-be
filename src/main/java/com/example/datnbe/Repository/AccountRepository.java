package com.example.datnbe.Repository;

import com.example.datnbe.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,String> {
    Optional<Account> findByUsername (String username);

    Optional<Account> findByUsernameAndType (String username, String type);

    Optional<Account> findByIdEmployee (String idEmployee);

    boolean existsByUsername(String username);

}
