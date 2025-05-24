package com.example.datnbe.Repository;

import com.example.datnbe.Entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, String>, JpaSpecificationExecutor<Categories> {
}