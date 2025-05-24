package com.example.datnbe.Resource;

import com.example.datnbe.Entity.Criteria.CategoriesCriteria;
import com.example.datnbe.Entity.DTO.CategoriesDTO;
import com.example.datnbe.Service.CategoryService;
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
public class CategoryResource {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/search-category")
    public ResponseEntity<List<CategoriesDTO>> searchCategory(@RequestBody CategoriesCriteria criteria, Pageable pageable) {
        Page<CategoriesDTO> page = categoryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/get-all-category")
    public ResponseEntity<List<CategoriesDTO>> getAllCategory() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping("/get-category-by-id")
    public ResponseEntity<CategoriesDTO> getCategoryById(@RequestParam String id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping("/create-category")
    public ResponseEntity<CategoriesDTO> createCategory(@RequestBody CategoriesDTO dto) {
        return ResponseEntity.ok(categoryService.createCategory(dto));
    }

    @PostMapping("/update-category")
    public ResponseEntity<CategoriesDTO> updateCategory(@RequestBody CategoriesDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(dto));
    }

    @DeleteMapping("/delete-category")
    public ResponseEntity<Void> deleteCategory(@RequestParam String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}
