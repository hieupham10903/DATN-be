package com.example.datnbe.Service;

import com.example.datnbe.Entity.Categories;
import com.example.datnbe.Entity.Criteria.ProductCriteria;
import com.example.datnbe.Entity.DTO.ProductCategoryStatisticDTO;
import com.example.datnbe.Entity.DTO.ProductsDTO;
import com.example.datnbe.Entity.Products;
import com.example.datnbe.Entity.Products_;
import com.example.datnbe.Entity.Warehouses;
import com.example.datnbe.Mapper.ProductMapper;
import com.example.datnbe.Repository.CategoriesRepository;
import com.example.datnbe.Repository.ProductRepository;
import com.example.datnbe.Repository.WarehouseRepository;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Watchable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService extends ArcQueryService<Products> {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoriesRepository categoriesRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;

    public Page<ProductsDTO> findByCriteria(ProductCriteria criteria, Pageable page) {
        final Specification<Products> specification = createSpecification(criteria);
        return productRepository.findAll(specification, page).map(productMapper::toDto);
    }

    protected Specification<Products> createSpecification(ProductCriteria criteria) {
        Specification<Products> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getName() != null && !"undefined".equals(criteria.getName().getContains())) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Products_.name));
            }
            if (criteria.getPrice() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getPrice(), Products_.price));
            }
            if (criteria.getCode() != null && !"undefined".equals(criteria.getCode().getEquals())) {
                specification = specification.and(buildStringSpecification(criteria.getCode(), Products_.code));
            }
            if (criteria.getCategoryId() != null && !"undefined".equals(criteria.getCategoryId().getEquals())) {
                specification = specification.and(buildStringSpecification(criteria.getCategoryId(), Products_.categoryId));
            }
            if (criteria.getWarehouseId() != null && !"undefined".equals(criteria.getWarehouseId().getEquals())) {
                specification = specification.and(buildStringSpecification(criteria.getWarehouseId(), Products_.warehouseId));
            }
        }
        return specification;
    }

    public List<ProductsDTO> getAllProduct() {
        List<Products> employees = productRepository.findAll();
        return employees.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    public ProductsDTO getProductById(String id) {
        Optional<Products> products = productRepository.findById(id);
        if (products.isEmpty()) {
            throw new ServiceException("Không tìm thấy sản phẩm");
        }
        return productMapper.toDto(products.get());
    }

    public ProductsDTO createProduct(ProductsDTO dto) {
        if (productRepository.existsByCode(dto.getCode())) {
            throw new ServiceException("Mã sản phẩm đã tồn tại");
        }

        Products product = productMapper.toEntity(dto);
        product.setId(UUID.randomUUID().toString());
        product.setCreatedAt(LocalDateTime.now());
        return productMapper.toDto(productRepository.save(product));
    }


    public ProductsDTO updateProduct(ProductsDTO dto) {
        Optional<Products> optionalProduct = productRepository.findById(dto.getId());
        if (optionalProduct.isEmpty()) {
            throw new ServiceException("Không tìm thấy sản phẩm");
        }

        if (productRepository.existsByCode(dto.getCode()) &&
                !optionalProduct.get().getCode().equals(dto.getCode())) {
            throw new ServiceException("Mã sản phẩm đã tồn tại");
        }

        Products existingProduct = optionalProduct.get();
        existingProduct.setName(dto.getName());
        existingProduct.setCode(dto.getCode());
        existingProduct.setDescription(dto.getDescription());
        existingProduct.setPrice(dto.getPrice());
        existingProduct.setStockQuantity(dto.getStockQuantity());
        existingProduct.setCategoryId(dto.getCategoryId());
        existingProduct.setWarehouseId(dto.getWarehouseId());
        existingProduct.setImageUrl(dto.getImageUrl());
        existingProduct.setImageDetail(dto.getImageDetail());

        return productMapper.toDto(productRepository.save(existingProduct));
    }


    public void deleteProduct(String id) {
        Optional<Products> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            throw new ServiceException("Không tìm thấy sản phẩm để xóa");
        }

        Products product = optionalProduct.get();

        if (product.getImageUrl() != null) {
            File mainImage = new File(product.getImageUrl());
            if (mainImage.exists()) mainImage.delete();
        }

        if (product.getImageDetail() != null && !product.getImageDetail().isEmpty()) {
            String[] detailImages = product.getImageDetail().split(",");
            for (String imagePath : detailImages) {
                File imageFile = new File(imagePath.trim());
                if (imageFile.exists()) imageFile.delete();
            }
        }

        productRepository.deleteById(id);
    }


    public List<ProductCategoryStatisticDTO> getProductStatisticWithCategoryName() {
        return productRepository.getProductStatisticByCategoryWithName();
    }

    public List<ProductsDTO> getAllProductInfo() {
        List<Products> products = productRepository.findAll();
        List<Categories> categories = categoriesRepository.findAll();
        List<Warehouses> warehouses = warehouseRepository.findAll();

        Map<String, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Categories::getId, Categories::getName));

        Map<String, String> warehouseMap = warehouses.stream()
                .collect(Collectors.toMap(Warehouses::getId, Warehouses::getName));

        List<ProductsDTO> list = productMapper.toDtoList(products);

        for (ProductsDTO dto : list) {
            dto.setCategoryName(categoryMap.get(dto.getCategoryId()));
            dto.setWarehouseName(warehouseMap.get(dto.getWarehouseId()));
        }

        return list;
    }
}
