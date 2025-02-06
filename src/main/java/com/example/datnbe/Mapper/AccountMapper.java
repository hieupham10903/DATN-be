package com.example.datnbe.Mapper;

import com.example.datnbe.Entity.Account;
import com.example.datnbe.Entity.DTO.AccountDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper extends EntityMapper<AccountDTO, Account> {
}
