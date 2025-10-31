package com.example.orderservice.controller;

import com.example.orderservice.client.StockServiceClient;
import com.example.orderservice.client.UserServiceClient;
import com.example.orderservice.dto.AddressDto;
import com.example.orderservice.dto.FrontendOrderRequest;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderItemDto;
import com.example.orderservice.dto.ProductDto;
import com.example.orderservice.dto.SizeDto;
import com.example.orderservice.dto.UpdateOrderRequest;
import com.example.orderservice.jwt.JwtUtil;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
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
    private final UserServiceClient userServiceClient;

    private OrderDto enrichOrderDto(Order order) {
        OrderDto dto = modelMapper.map(order, OrderDto.class);
        
        // Set addressId from order
        if (order.getAddressId() != null && !order.getAddressId().isBlank()) {
            dto.setAddressId(order.getAddressId());
            
            // Fetch and enrich address data
            try {
                ResponseEntity<AddressDto> addressResponse = userServiceClient.getAddressById(order.getAddressId());
                if (addressResponse != null && addressResponse.getBody() != null) {
                    AddressDto address = addressResponse.getBody();
                    
                    // Set recipient phone
                    if (address.getRecipientPhone() != null && !address.getRecipientPhone().isBlank()) {
                        dto.setRecipientPhone(address.getRecipientPhone());
                    }
                    
                    // Build full address string
                    StringBuilder fullAddressBuilder = new StringBuilder();
                    if (address.getStreetAddress() != null && !address.getStreetAddress().isBlank()) {
                        fullAddressBuilder.append(address.getStreetAddress());
                    }
                    if (address.getProvince() != null && !address.getProvince().isBlank()) {
                        if (fullAddressBuilder.length() > 0) {
                            fullAddressBuilder.append(", ");
                        }
                        fullAddressBuilder.append(address.getProvince());
                    }
                    if (fullAddressBuilder.length() > 0) {
                        dto.setFullAddress(fullAddressBuilder.toString());
                    }
                    
                    // If shippingAddress is not set yet, use fullAddress
                    if ((dto.getShippingAddress() == null || dto.getShippingAddress().isBlank()) && dto.getFullAddress() != null) {
                        dto.setShippingAddress(dto.getFullAddress());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch address for addressId: " + order.getAddressId() + " - " + e.getMessage());
            }
        }

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemDto> enrichedItems = order.getOrderItems().stream()
                    .map(item -> {
                        OrderItemDto itemDto = modelMapper.map(item, OrderItemDto.class);

                        if (item.getProductId() != null && !item.getProductId().isBlank()) {
                            try {
                                ResponseEntity<ProductDto> productResponse = stockServiceClient.getProductById(item.getProductId());
                                if (productResponse != null && productResponse.getBody() != null) {
                                    ProductDto product = productResponse.getBody();
                                    if (product.getName() != null) {
                                        itemDto.setProductName(product.getName());
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Failed to fetch product name for productId: " + item.getProductId() + " - " + e.getMessage());
                            }
                        }

                        if (item.getSizeId() != null && !item.getSizeId().isBlank()) {
                            try {
                                ResponseEntity<SizeDto> sizeResponse = stockServiceClient.getSizeById(item.getSizeId());
                                if (sizeResponse != null && sizeResponse.getBody() != null) {
                                    SizeDto size = sizeResponse.getBody();
                                    if (size.getName() != null) {
                                        itemDto.setSizeName(size.getName());
                                    }
                                }
                            } catch (Exception e) {
                                // Log error but don't fail the request
                                System.err.println("Failed to fetch size name for sizeId: " + item.getSizeId() + " - " + e.getMessage());
                            }
                        }

                        return itemDto;
                    })
                    .collect(Collectors.toList());
            dto.setOrderItems(enrichedItems);
        }

        return dto;
    }

    // Helper method để enrich List<Order>
    private List<OrderDto> enrichOrderDtos(List<Order> orders) {
        return orders.stream()
                .map(this::enrichOrderDto)
                .collect(Collectors.toList());
    }

    // Helper method để enrich Page<Order>
    private Page<OrderDto> enrichOrderDtosPage(Page<Order> ordersPage) {
        return ordersPage.map(this::enrichOrderDto);
    }

    // ========== Existing Endpoints ==========

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

    // Refactor existing endpoint để dùng enrichOrderDto
    @GetMapping("/getOrderByUserId")
    public ResponseEntity<List<OrderDto>> getOrderByUserId(HttpServletRequest request) {
        String userId = jwtUtil.ExtractUserId(request);
        List<Order> orders = orderService.getUserOrders(userId);

        // Dùng enrichOrderDto để có productName và sizeName
        List<OrderDto> orderDtos = enrichOrderDtos(orders);

        return ResponseEntity.ok(orderDtos);
    }

    // ========== New Shop Owner Endpoints ==========
    @GetMapping("/shop-owner/orders")
    public ResponseEntity<Page<OrderDto>> getOrdersByShopOwner(
            HttpServletRequest request,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        try {
            String shopOwnerId = jwtUtil.ExtractUserId(request);
            // Get orders paginated
            Page<Order> ordersPage = orderService.getOrdersByShopOwner(shopOwnerId, status, pageNo, pageSize);
            // Enrich với productName và sizeName
            Page<OrderDto> orderDtosPage = enrichOrderDtosPage(ordersPage);

            return ResponseEntity.ok(orderDtosPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/shop-owner/orders/all")
    public ResponseEntity<List<OrderDto>> getAllOrdersByShopOwner(
            HttpServletRequest request,
            @RequestParam(required = false) String status) {

        try {
            String shopOwnerId = jwtUtil.ExtractUserId(request);
            List<Order> orders = orderService.getOrdersByShopOwner(shopOwnerId, status);

            //  với productName và sizeName
            List<OrderDto> orderDtos = enrichOrderDtos(orders);

            return ResponseEntity.ok(orderDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/shop-owner/orders/{orderId}")
    public ResponseEntity<OrderDto> getOrderByIdForShopOwner(
            @PathVariable String orderId,
            HttpServletRequest request) {

        try {
            String shopOwnerId = jwtUtil.ExtractUserId(request);

            // Get order
            Order order = orderService.getOrderById(orderId);

            // Verify this order contains products belonging to this shop owner
            List<String> productIds = stockServiceClient.getProductIdsByShopOwner(shopOwnerId).getBody();

            if (productIds == null || productIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            boolean belongsToShopOwner = order.getOrderItems().stream()
                    .anyMatch(item -> productIds.contains(item.getProductId()));

            if (!belongsToShopOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            // Enrich với productName và sizeName
            OrderDto dto = enrichOrderDto(order);

            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ========== Alternative Endpoints (không enrich - nhanh hơn) ==========
    // Nếu cần performance và không cần productName/sizeName, có thể thêm endpoints này:

    /**
     * Get orders for shop owner (paginated) - WITHOUT enrichment (faster)
     * Chỉ dùng ModelMapper trực tiếp, không gọi external service
     */
    @GetMapping("/shop-owner/orders/simple")
    public ResponseEntity<Page<OrderDto>> getOrdersByShopOwnerSimple(
            HttpServletRequest request,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        try {
            String shopOwnerId = jwtUtil.ExtractUserId(request);
            Page<Order> ordersPage = orderService.getOrdersByShopOwner(shopOwnerId, status, pageNo, pageSize);

            // Map trực tiếp bằng ModelMapper - không enrich (nhanh hơn)
            Page<OrderDto> orderDtosPage = ordersPage.map(order -> modelMapper.map(order, OrderDto.class));

            return ResponseEntity.ok(orderDtosPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PutMapping("/shop-owner/orders/{orderId}/status")
    public ResponseEntity<OrderDto> updateOrderStatusForShopOwner(
            @PathVariable String orderId,
            @RequestParam String status,
            HttpServletRequest request) {
        
        try {
            String shopOwnerId = jwtUtil.ExtractUserId(request);

            Order order = orderService.getOrderById(orderId);
            List<String> productIds = stockServiceClient.getProductIdsByShopOwner(shopOwnerId).getBody();
            
            if (productIds == null || productIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            boolean belongsToShopOwner = order.getOrderItems().stream()
                    .anyMatch(item -> productIds.contains(item.getProductId()));
            
            if (!belongsToShopOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            // Update order status
            Order updatedOrder = orderService.updateOrderStatus(orderId, status);
            
            // Return enriched DTO
            OrderDto dto = enrichOrderDto(updatedOrder);
            
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Update order (full update with UpdateOrderRequest)
     */
    @PutMapping("/shop-owner/orders/{orderId}")
    public ResponseEntity<OrderDto> updateOrderForShopOwner(
            @PathVariable String orderId,
            @RequestBody UpdateOrderRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            String shopOwnerId = jwtUtil.ExtractUserId(httpRequest);
            
            // Verify order belongs to shop owner
            Order order = orderService.getOrderById(orderId);
            List<String> productIds = stockServiceClient.getProductIdsByShopOwner(shopOwnerId).getBody();
            
            if (productIds == null || productIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            boolean belongsToShopOwner = order.getOrderItems().stream()
                    .anyMatch(item -> productIds.contains(item.getProductId()));
            
            if (!belongsToShopOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            // Update order
            Order updatedOrder = orderService.updateOrder(orderId, request);
            
            // Return enriched DTO
            OrderDto dto = enrichOrderDto(updatedOrder);
            
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}