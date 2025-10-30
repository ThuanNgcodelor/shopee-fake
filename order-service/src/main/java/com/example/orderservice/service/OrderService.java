package com.example.orderservice.service;

import com.example.orderservice.dto.AddressDto;
import com.example.orderservice.dto.FrontendOrderRequest;
import com.example.orderservice.dto.UpdateOrderRequest;
import com.example.orderservice.model.Order;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface OrderService {
    Order getOrderById(String orderId);
    List<Order> getUserOrders(String userId);
    // CRUD methods
    List<Order> getAllOrders(String status);
    Order updateOrder(String orderId, UpdateOrderRequest request);
    Order updateOrderStatus(String orderId, String status);
    void deleteOrder(String orderId);
    List<Order> searchOrders(String userId, String status, String startDate, String endDate);
    // Address methods
    List<AddressDto> getUserAddresses(HttpServletRequest request);
    AddressDto getAddressById(String addressId);
    // Frontend order creation
    void orderByKafka(FrontendOrderRequest orderRequest, HttpServletRequest request);
}