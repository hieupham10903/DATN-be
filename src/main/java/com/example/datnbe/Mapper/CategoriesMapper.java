package com.example.datnbe.Mapper;

import com.example.datnbe.Entity.Categories;
import com.example.datnbe.Entity.DTO.CategoriesDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoriesMapper extends EntityMapper<CategoriesDTO, Categories> {
}
