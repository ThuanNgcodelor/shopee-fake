package com.example.stockservice.repository;

import com.example.stockservice.enums.ProductStatus;
import com.example.stockservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,String> {
    @Query("SELECT p FROM products p WHERE p.name LIKE CONCAT('%', :keyword, '%')")
    List<Product> searchProductByName(@Param("keyword") String keyword);
    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);
    List<Product> findByUserId(String userId);
    @Query("SELECT DISTINCT p FROM products p LEFT JOIN FETCH p.sizes where p.status = 'IN_STOCK'")
    List<Product> findAllWithSizes();
}
