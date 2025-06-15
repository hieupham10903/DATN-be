package com.example.datnbe.Mapper;

import com.example.datnbe.Entity.DTO.EmployeeDTO;
import com.example.datnbe.Entity.Employee;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeMapper extends EntityMapper<EmployeeDTO, Employee> {
}
