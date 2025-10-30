package com.example.orderservice.controller;

import com.example.orderservice.client.StockServiceClient;
import com.example.orderservice.dto.AddressDto;
import com.example.orderservice.dto.FrontendOrderRequest;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderItemDto;
import com.example.orderservice.dto.ProductDto;
import com.example.orderservice.dto.SizeDto;
import com.example.orderservice.jwt.JwtUtil;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final ModelMapper modelMapper;
    private final JwtUtil jwtUtil;
    private final StockServiceClient stockServiceClient;

    @PostMapping("/create-from-cart")
    ResponseEntity<?> createOrderFromCart(@RequestBody FrontendOrderRequest request, HttpServletRequest httpRequest) {
        try {
            orderService.orderByKafka(request, httpRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Order has been sent to Kafka.",
                    "status", "PENDING"
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Insufficient stock")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", "INSUFFICIENT_STOCK",
                        "message", e.getMessage(),
                        "details", extractStockDetails(e.getMessage())
                ));
            }
            if (e.getMessage().contains("Address not found")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", "ADDRESS_NOT_FOUND",
                        "message", e.getMessage()
                ));
            }
            if (e.getMessage().contains("Cart not found") || e.getMessage().contains("empty")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", "CART_EMPTY",
                        "message", e.getMessage()
                ));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "ORDER_FAILED",
                    "message", e.getMessage()
            ));
        }
    }

    private Map<String, Object> extractStockDetails(String message) {
        Map<String, Object> details = new HashMap<>();
        if (message.contains("Available:") && message.contains("Requested:")) {
            String[] parts = message.split("Available:|Requested:");
            if (parts.length >= 3) {
                details.put("available", parts[1].trim().replaceAll("[^0-9]", ""));
                details.put("requested", parts[2].trim().replaceAll("[^0-9]", ""));
            }
        }
        return details;
    }

    @GetMapping("/addresses")
    ResponseEntity<List<AddressDto>> getUserAddresses(HttpServletRequest request) {
        List<AddressDto> addresses = orderService.getUserAddresses(request);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/addresses/{addressId}")
    ResponseEntity<AddressDto> getAddressById(@PathVariable String addressId) {
        AddressDto address = orderService.getAddressById(addressId);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/getOrderByUserId")
    public ResponseEntity<List<OrderDto>> getOrderByUserId(HttpServletRequest request) {
        String userId = jwtUtil.ExtractUserId(request);
        List<Order> orders = orderService.getUserOrders(userId);
        List<OrderDto> orderDtos = orders.stream()
                .map(order -> {
                    OrderDto dto = modelMapper.map(order, OrderDto.class);
                    
                    if (dto.getOrderItems() != null && !dto.getOrderItems().isEmpty()) {
                        for (OrderItemDto itemDto : dto.getOrderItems()) {
                            if (itemDto.getProductId() != null && !itemDto.getProductId().isBlank()) {
                                ResponseEntity<ProductDto> productResponse = stockServiceClient.getProductById(itemDto.getProductId());
                                if (productResponse != null && productResponse.getBody() != null) {
                                    ProductDto product = productResponse.getBody();
                                    if (product.getName() != null) {
                                            itemDto.setProductName(product.getName());
                                    }
                                }

                            }
                            if (itemDto.getSizeId() != null && !itemDto.getSizeId().isBlank()) {
                                    ResponseEntity<SizeDto> sizeResponse = stockServiceClient.getSizeById(itemDto.getSizeId());
                                    if (sizeResponse != null && sizeResponse.getBody() != null) {
                                        SizeDto size = sizeResponse.getBody();
                                        if (size.getName() != null) {
                                            itemDto.setSizeName(size.getName());
                                        }
                                    }
                            }
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDtos);
    }
}
