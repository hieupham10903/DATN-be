package com.example.datnbe.Resource;

import com.example.datnbe.Entity.Criteria.WarehouseCriteria;
import com.example.datnbe.Entity.DTO.WarehousesDTO;
import com.example.datnbe.Service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WarehouseResource {

    @Autowired
    private WarehouseService warehouseService;

    @PostMapping("/search-warehouse")
    public ResponseEntity<List<WarehousesDTO>> searchWarehouse(@RequestBody WarehouseCriteria criteria, Pageable pageable) {
        Page<WarehousesDTO> page = warehouseService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/get-all-warehouse")
    public ResponseEntity<List<WarehousesDTO>> getAllWarehouse() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @PostMapping("/get-warehouse-by-id")
    public ResponseEntity<WarehousesDTO> getWarehouseById(@RequestParam String id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @PostMapping("/create-warehouse")
    public ResponseEntity<WarehousesDTO> createWarehouse(@RequestBody WarehousesDTO dto) {
        return ResponseEntity.ok(warehouseService.createWarehouse(dto));
    }

    @PostMapping("/update-warehouse")
    public ResponseEntity<WarehousesDTO> updateWarehouse(@RequestBody WarehousesDTO dto) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(dto));
    }

    @PostMapping("/delete-warehouse")
    public ResponseEntity<Void> deleteWarehouse(@RequestParam String id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok().build();
    }
}
