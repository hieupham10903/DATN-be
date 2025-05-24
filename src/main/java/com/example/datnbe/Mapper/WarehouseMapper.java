package com.example.datnbe.Mapper;

import com.example.datnbe.Entity.DTO.WarehousesDTO;
import com.example.datnbe.Entity.Warehouses;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarehouseMapper extends EntityMapper<WarehousesDTO, Warehouses> {
}
