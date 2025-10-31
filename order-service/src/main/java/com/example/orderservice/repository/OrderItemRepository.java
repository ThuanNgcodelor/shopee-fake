package com.example.orderservice.repository;

import com.example.orderservice.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findByProductIdIn(List<String> productId);
    @Query("SELECT DISTINCT oi.order.id FROM OrderItem oi WHERE oi.productId IN :productIds")
    Set<OrderItem> findDistinctOrderIdsByProductIdIn(@Param("productIds") List<String> productIds);
    List<OrderItem> findByProductId(String productId);
}
