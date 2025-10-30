package com.example.orderservice.repository;

import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdOrderByCreationTimestampDesc(String userId);
    List<Order> findByOrderStatus(OrderStatus orderStatus);
    List<Order> findByCreationTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
}
