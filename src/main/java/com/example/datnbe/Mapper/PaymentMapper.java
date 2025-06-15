package com.example.datnbe.Mapper;

import com.example.datnbe.Entity.DTO.PaymentsDTO;
import com.example.datnbe.Entity.Payments;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper extends EntityMapper<PaymentsDTO, Payments> {
}
