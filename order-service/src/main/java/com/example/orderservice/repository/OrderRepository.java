package com.example.orderservice.repository;

import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdOrderByCreationTimestampDesc(String userId);
    List<Order> findByOrderStatus(OrderStatus orderStatus);
    List<Order> findByCreationTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    @Query("select DISTINCT o from Order o " +
            "inner join o.orderItems oi " +
            "where oi.productId in :productIds")
    List<Order> findByOrderItemsProductIdIn(@Param("productIds") List<String> productIds);
    @Query("SELECT DISTINCT o FROM Order o " +
            "INNER JOIN o.orderItems oi " +
            "WHERE oi.productId IN :productIds")
    Page<Order> findByOrderItemsProductIdIn(@Param("productIds") List<String> productIds, Pageable pageable);
    @Query("SELECT DISTINCT o FROM Order o" +
            " INNER JOIN o.orderItems oi " +
            "WHERE oi.productId IN :productIds " +
            "AND (:status IS NULL OR o.orderStatus = :status) " +
            "ORDER BY o.creationTimestamp DESC")
    Page<Order> findByShopOwnerProducts(@Param("productIds") List<String> productIds, Pageable pageable, OrderStatus status);
}
