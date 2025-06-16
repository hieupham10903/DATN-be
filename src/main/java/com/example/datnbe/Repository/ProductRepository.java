package com.example.datnbe.Repository;

import com.example.datnbe.Entity.DTO.ProductCategoryStatisticDTO;
import com.example.datnbe.Entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Products, String>, JpaSpecificationExecutor<Products> {
    @Query("""
                SELECT new com.example.datnbe.Entity.DTO.ProductCategoryStatisticDTO(
                    c.id, c.name, COUNT(p)
                )
                FROM Products p
                JOIN Categories c ON p.categoryId = c.id
                GROUP BY c.id, c.name
            """)
    List<ProductCategoryStatisticDTO> getProductStatisticByCategoryWithName();

    Products findNameById(String id);

}