package com.example.stockservice.repository;

import com.example.stockservice.model.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size, String> {
    List<Size> findByProductId(String productId);
    Size findByProductIdAndName(String productId, String name);
}

