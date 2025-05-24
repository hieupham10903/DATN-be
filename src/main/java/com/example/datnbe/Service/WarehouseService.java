package com.example.datnbe.Service;

import com.example.datnbe.Entity.Criteria.WarehouseCriteria;
import com.example.datnbe.Entity.DTO.WarehousesDTO;
import com.example.datnbe.Entity.Warehouses;
import com.example.datnbe.Entity.Warehouses_;
import com.example.datnbe.Mapper.WarehouseMapper;
import com.example.datnbe.Repository.WarehouseRepository;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarehouseService extends ArcQueryService<Warehouses> {
    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private WarehouseMapper warehouseMapper;

    public Page<WarehousesDTO> findByCriteria(WarehouseCriteria criteria, Pageable page) {
        final Specification<Warehouses> specification = createSpecification(criteria);
        return warehouseRepository.findAll(specification, page).map(warehouseMapper::toDto);
    }

    protected Specification<Warehouses> createSpecification(WarehouseCriteria criteria) {
        Specification<Warehouses> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getName() != null && !"undefined".equals(criteria.getName().getContains())) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Warehouses_.name));
            }
            if (criteria.getCode() != null && !"undefined".equals(criteria.getCode().getEquals())) {
                specification = specification.and(buildStringSpecification(criteria.getCode(), Warehouses_.code));
            }
        }
        return specification;
    }

    public List<WarehousesDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(warehouseMapper::toDto)
                .collect(Collectors.toList());
    }

    public WarehousesDTO getWarehouseById(String id) {
        Optional<Warehouses> optional = warehouseRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ServiceException("Không tìm thấy kho");
        }
        return warehouseMapper.toDto(optional.get());
    }

    public WarehousesDTO createWarehouse(WarehousesDTO dto) {
        Warehouses warehouse = warehouseMapper.toEntity(dto);
        warehouse.setId(UUID.randomUUID().toString());
        return warehouseMapper.toDto(warehouseRepository.save(warehouse));
    }

    public WarehousesDTO updateWarehouse(WarehousesDTO dto) {
        Optional<Warehouses> optional = warehouseRepository.findById(dto.getId());
        if (optional.isEmpty()) {
            throw new ServiceException("Không tìm thấy kho");
        }

        Warehouses existing = optional.get();
        existing.setName(dto.getName());
        existing.setLocation(dto.getLocation());
        existing.setCode(dto.getCode());

        return warehouseMapper.toDto(warehouseRepository.save(existing));
    }

    public void deleteWarehouse(String id) {
        if (!warehouseRepository.existsById(id)) {
            throw new ServiceException("Không tìm thấy kho để xóa");
        }
        warehouseRepository.deleteById(id);
    }
}
