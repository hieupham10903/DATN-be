package com.example.datnbe.Mapper;

import com.example.datnbe.Entity.DTO.OrdersDTO;
import com.example.datnbe.Entity.Orders;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper extends EntityMapper<OrdersDTO, Orders> {
}
