package com.example.orderservice.client;

import com.example.orderservice.config.FeignConfig;
import com.example.orderservice.dto.CartDto;
import com.example.orderservice.dto.ProductDto;
import com.example.orderservice.dto.SizeDto;
import com.example.orderservice.request.DecreaseStockRequest;
import com.example.orderservice.request.RemoveCartItemRequest;
import com.example.orderservice.request.RemoveCartItemByUserIdRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "stock-service", path = "/v1/stock",configuration = FeignConfig.class)
public interface StockServiceClient {

    @PostMapping(value = "/cart/removeItems", headers = "X-Internal-Call=true")
    ResponseEntity<Void> removeCartItems(@RequestBody RemoveCartItemRequest request);

    @PostMapping(value = "/product/decreaseStock", headers = "X-Internal-Call=true")
    ProductDto decreaseStock(@RequestBody DecreaseStockRequest request);

    @GetMapping("/cart/user")
    ResponseEntity<CartDto> getCart(@RequestHeader("Authorization") String token);

    @GetMapping("/cart/getCartByUserId")
    ResponseEntity<CartDto> getCartByUserId(HttpServletRequest request);

    @GetMapping(value = "/cart/getCartByUserId/{userId}", headers = "X-Internal-Call=true")
    ResponseEntity<CartDto> getCartByUserIdInternal(@PathVariable String userId);

    @GetMapping(value = "/product/getProductById/{id}",headers = "X-Internal-Call=true")
    ResponseEntity<ProductDto> getProductById(@PathVariable String id);

    @GetMapping(value = "/size/getSizeById/{sizeId}",headers = "X-Internal-Call=true")
    ResponseEntity<SizeDto> getSizeById(@PathVariable String sizeId);

    @DeleteMapping(value = "/cart/clear/{cartId}",headers = "X-Internal-Call=true")
    ResponseEntity<Void> clearCartByCartId(@PathVariable String cartId);

    @PostMapping(value = "/cart/removeItemsByUserId", headers = "X-Internal-Call=true")
    ResponseEntity<Void> removeCartItemsByUserId(@RequestBody RemoveCartItemByUserIdRequest request);
}
