package com.example.datnbe.Mapper;

import com.example.datnbe.Entity.DTO.ProductsDTO;
import com.example.datnbe.Entity.Products;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper extends EntityMapper<ProductsDTO, Products> {
}
