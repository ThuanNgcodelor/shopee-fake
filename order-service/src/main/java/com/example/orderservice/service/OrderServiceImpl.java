package com.example.orderservice.service;

import com.example.orderservice.client.StockServiceClient;
import com.example.orderservice.client.UserServiceClient;
import com.example.orderservice.dto.*;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.request.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    // Get data for shop owner orders
    @Override
    public Page<Order> getOrdersByShopOwner(String shopOwnerId, String status, Integer pageNo, Integer pageSize) {
        // 1. Get all productIds belonging to this shop owner from stock-service
        List<String> productIds = stockServiceClient.getProductIdsByShopOwner(shopOwnerId).getBody();

        if (productIds == null || productIds.isEmpty()) {
            return Page.empty(); // No products = no orders
        }

        // 2. Query orders that have orderItems with these productIds
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        OrderStatus orderStatus = (status != null && !status.isEmpty())
                ? OrderStatus.valueOf(status.toUpperCase())
                : null;

        return orderRepository.findByShopOwnerProducts(productIds,pageable, orderStatus);
    }

    @Override
    public List<Order> getOrdersByShopOwner(String shopOwnerId, String status) {
        List<String> productIds = stockServiceClient.getProductIdsByShopOwner(shopOwnerId).getBody();
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        return orderRepository.findByOrderItemsProductIdIn(productIds);
    }

    private final OrderRepository orderRepository;
    private final StockServiceClient stockServiceClient;
    private final UserServiceClient userServiceClient;
    private final NewTopic orderTopic;
    private final NewTopic notificationTopic;
    private final KafkaTemplate<String, CheckOutKafkaRequest> kafkaTemplate;
    private final KafkaTemplate<String, SendNotificationRequest> kafkaTemplateSend;
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);


    ///////////////////////////////////////////////////////////////////////////////////
    @Override
    @Transactional
    public void orderByKafka(FrontendOrderRequest orderRequest, HttpServletRequest request){
        String author = request.getHeader("Authorization");
        CartDto cartDto = stockServiceClient.getCart(author).getBody();
        AddressDto address = userServiceClient.getAddressById(orderRequest.getAddressId()).getBody();
        if (address == null)
            throw new RuntimeException("Address not found for ID: " + orderRequest.getAddressId());
        if(cartDto == null || cartDto.getItems().isEmpty())
            throw new RuntimeException("Cart not found or empty");

        for (SelectedItemDto item : orderRequest.getSelectedItems()) {
            if (item.getSizeId() == null || item.getSizeId().isBlank()) {
                throw new RuntimeException("Size ID is required for product: " + item.getProductId());
            }
            
            ProductDto product = stockServiceClient.getProductById(item.getProductId()).getBody();
            if (product == null) {
                throw new RuntimeException("Product not found for ID: " + item.getProductId());
            }
            
            SizeDto size = stockServiceClient.getSizeById(item.getSizeId()).getBody();
            if (size == null) {
                throw new RuntimeException("Size not found for ID: " + item.getSizeId());
            }
            
            if (size.getStock() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName()
                        + ", size: " + size.getName() + ". Available: " + size.getStock() + ", Requested: " + item.getQuantity());
            }
        }

        CheckOutKafkaRequest kafkaRequest = CheckOutKafkaRequest.builder()
                .userId(cartDto.getUserId())
                .addressId(orderRequest.getAddressId())
                .cartId(cartDto.getId())
                .selectedItems(orderRequest.getSelectedItems())
                .build();

        kafkaTemplate.send(orderTopic.name(), kafkaRequest);
    }

    @KafkaListener(topics = "#{@orderTopic.name}", groupId = "order-service-checkout")
    @Transactional
    public void consumeCheckout(CheckOutKafkaRequest msg) {
        if (msg.getAddressId() == null || msg.getAddressId().isBlank()) {
            throw new RuntimeException("addressId is required in message");
        }
        if (msg.getSelectedItems() == null || msg.getSelectedItems().isEmpty()) {
            return;
        }
        try {
            for (SelectedItemDto item : msg.getSelectedItems()) {
                if (item.getSizeId() == null || item.getSizeId().isBlank()) {
                    throw new RuntimeException("Size ID is required for product: " + item.getProductId());
                }
                
                ProductDto product = stockServiceClient.getProductById(item.getProductId()).getBody();
                if (product == null) {
                    log.error("[CONSUMER] Product not found: {}", item.getProductId());
                    throw new RuntimeException("Product not found for ID: " + item.getProductId());
                }
                
                SizeDto size = stockServiceClient.getSizeById(item.getSizeId()).getBody();
                if (size == null) {
                    log.error("[CONSUMER] Size not found: {}", item.getSizeId());
                    throw new RuntimeException("Size not found for ID: " + item.getSizeId());
                }
                
                if (size.getStock() < item.getQuantity()) {
                    log.error("[CONSUMER] Insufficient stock for product {} size {}. Available: {}, Requested: {}",
                            item.getProductId(), size.getName(), size.getStock(), item.getQuantity());
                    throw new RuntimeException("Insufficient stock for product: " + product.getName()
                            + ", size: " + size.getName() + ". Available: " + size.getStock() + ", Requested: " + item.getQuantity());
                }
            }
        } catch (Exception e) {
            try {
                SendNotificationRequest failNotification = SendNotificationRequest.builder()
                        .userId(msg.getUserId())
                        .orderId(null)
                        .message("Order creation failed: " + e.getMessage())
                        .build();
                kafkaTemplateSend.send(notificationTopic.name(), failNotification);
            } catch (Exception notifEx) {
                log.error("[CONSUMER] Failed to send failure notification: {}", notifEx.getMessage(), notifEx);
            }
            throw e;
        }

        // 1) Create order skeleton
        Order order = initPendingOrder(msg.getUserId(), msg.getAddressId());

        // 2) Items + decrease stock
        List<OrderItem> items = toOrderItemsFromSelected(msg.getSelectedItems(), order);
        order.setOrderItems(items);
        order.setTotalPrice(calculateTotalPrice(items));
        orderRepository.save(order);

        // 3) Cleanup cart - remove items that were added to order
        try {
            if (msg.getSelectedItems() != null && !msg.getSelectedItems().isEmpty()) {
                log.info("[CONSUMER] Starting cart cleanup for userId: {}, items count: {}", 
                    msg.getUserId(), msg.getSelectedItems().size());
                cleanupCartItemsBySelected(msg.getUserId(), msg.getSelectedItems());
            } else {
                log.warn("[CONSUMER] selectedItems is null or empty -> skip cart cleanup");
            }
        } catch (Exception e) {
            log.error("[CONSUMER] cart cleanup failed: {}", e.getMessage(), e);
        }

        try {
            notifyOrderPlaced(order);
        } catch (Exception e) {
            log.error("[CONSUMER] send notification failed: {}", e.getMessage(), e);
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////

    protected double calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }

    @Override
    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found for ID: " + orderId));
    }

    @Override
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreationTimestampDesc(userId);
    }

    // CRUD Implementation
    @Override
    public List<Order> getAllOrders(String status) {
        if (status != null && !status.isEmpty()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByOrderStatus(orderStatus);
        }
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public Order updateOrder(String orderId, UpdateOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found for ID: " + orderId));

        if (request.getOrderStatus() != null) {
            order.setOrderStatus(OrderStatus.valueOf(request.getOrderStatus().toUpperCase()));
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found for ID: " + orderId));

        order.setOrderStatus(OrderStatus.valueOf(status.toUpperCase()));
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found for ID: " + orderId));
        orderRepository.delete(order);
    }

    @Override
    public List<Order> searchOrders(String userId, String status, String startDate, String endDate) {
        if (userId != null && !userId.isEmpty()) {
            return orderRepository.findByUserIdOrderByCreationTimestampDesc (userId);
        }

        if (status != null && !status.isEmpty()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByOrderStatus(orderStatus);
        }

        return orderRepository.findAll();
    }

    // Address-related methods
    @Override
    public List<AddressDto> getUserAddresses(HttpServletRequest request) {
        String author = request.getHeader("Authorization");
        return userServiceClient.getAllAddresses(author).getBody();
    }

    @Override
    public AddressDto getAddressById(String addressId) {
        return userServiceClient.getAddressById(addressId).getBody();
    }


    private String selectDefaultAddress(List<AddressDto> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            throw new RuntimeException("No addresses found for user");
        }

        for (AddressDto address : addresses) {
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                return address.getAddressId();
            }
        }

        return addresses.get(0).getAddressId();
    }

    // ====== Helpers Tạo order ======
    private Order initPendingOrder(String userId, String addressId) {
        Order order = Order.builder()
                .userId(userId)
                .addressId(addressId)
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(0.0)
                .build();
        return orderRepository.save(order);
    }

    private List<OrderItem> toOrderItemsFromSelected(List<SelectedItemDto> selectedItems, Order order) {
        return selectedItems.stream()
                .map(si -> {
                    if (si.getSizeId() == null || si.getSizeId().isBlank()) {
                        throw new RuntimeException("Size ID is required for product: " + si.getProductId());
                    }
                    
                    DecreaseStockRequest dec = new DecreaseStockRequest();
                    dec.setSizeId(si.getSizeId());
                    dec.setQuantity(si.getQuantity());
                    stockServiceClient.decreaseStock(dec);

                    return OrderItem.builder()
                            .productId(si.getProductId())
                            .sizeId(si.getSizeId())
                            .quantity(si.getQuantity())
                            .unitPrice(si.getUnitPrice())
                            .totalPrice(si.getUnitPrice() * si.getQuantity())
                            .order(order)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void cleanupCartItemsBySelected(String userId, List<SelectedItemDto> selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            log.warn("[CONSUMER] selectedItems is null or empty - skipping cart cleanup");
            return;
        }
        
        log.info("[CONSUMER] Starting cart cleanup for userId: {}, number of items to remove: {}", 
            userId, selectedItems.size());
        
        for (SelectedItemDto item : selectedItems) {
            try {
                log.info("[CONSUMER] Removing cart item - productId: {}, sizeId: {}, quantity: {}", 
                    item.getProductId(), item.getSizeId(), item.getQuantity());
                
                RemoveCartItemByUserIdRequest request = new RemoveCartItemByUserIdRequest();
                request.setUserId(userId);
                request.setProductId(item.getProductId());
                request.setSizeId(item.getSizeId());
                
                log.debug("[CONSUMER] Sending removeCartItemsByUserId request - userId: {}, productId: {}, sizeId: {}", 
                    request.getUserId(), request.getProductId(), request.getSizeId());
                
                stockServiceClient.removeCartItemsByUserId(request);
                
                log.info("[CONSUMER] Successfully removed cart item - productId: {}, sizeId: {}", 
                    item.getProductId(), item.getSizeId());
            } catch (Exception e) {
                log.error("[CONSUMER] Failed to remove cart item - productId: {}, sizeId: {}, error: {}", 
                    item.getProductId(), item.getSizeId(), e.getMessage(), e);
            }
        }
    }

    private void notifyOrderPlaced(Order order) {
        SendNotificationRequest noti = SendNotificationRequest.builder()
                .userId(order.getUserId())
                .orderId(order.getId())
                .message("Order placed successfully with ID: " + order.getId())
                .build();
        kafkaTemplateSend.send(notificationTopic.name(), noti);
    }
}
