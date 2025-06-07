package com.example.datnbe.Mapper;

import com.example.datnbe.Entity.DTO.OrderItemsDTO;
import com.example.datnbe.Entity.OrderItems;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemsMapper extends EntityMapper<OrderItemsDTO, OrderItems> {
}
