package com.example.datnbe.Service;

import com.example.datnbe.Entity.Categories;
import com.example.datnbe.Entity.Categories_;
import com.example.datnbe.Entity.Criteria.CategoriesCriteria;
import com.example.datnbe.Entity.DTO.CategoriesDTO;
import com.example.datnbe.Mapper.CategoriesMapper;
import com.example.datnbe.Repository.CategoriesRepository;
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
public class CategoryService extends ArcQueryService<Categories> {

    @Autowired
    private CategoriesRepository categoryRepository;

    @Autowired
    private CategoriesMapper categoryMapper;

    public Page<CategoriesDTO> findByCriteria(CategoriesCriteria criteria, Pageable page) {
        Specification<Categories> specification = createSpecification(criteria);
        return categoryRepository.findAll(specification, page).map(categoryMapper::toDto);
    }

    protected Specification<Categories> createSpecification(CategoriesCriteria criteria) {
        Specification<Categories> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getName() != null && !"undefined".equals(criteria.getName().getContains())) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Categories_.name));
            }
        }
        return specification;
    }

    public List<CategoriesDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public CategoriesDTO getCategoryById(String id) {
        Optional<Categories> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            throw new ServiceException("Không tìm thấy danh mục");
        }
        return categoryMapper.toDto(category.get());
    }

    public CategoriesDTO createCategory(CategoriesDTO dto) {
        Categories category = categoryMapper.toEntity(dto);
        category.setId(UUID.randomUUID().toString());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    public CategoriesDTO updateCategory(CategoriesDTO dto) {
        Optional<Categories> optional = categoryRepository.findById(dto.getId());
        if (optional.isEmpty()) {
            throw new ServiceException("Không tìm thấy danh mục");
        }

        Categories category = optional.get();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        return categoryMapper.toDto(categoryRepository.save(category));
    }

    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new ServiceException("Không tìm thấy danh mục để xóa");
        }
        categoryRepository.deleteById(id);
    }
}
