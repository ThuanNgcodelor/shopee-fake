package com.example.stockservice.client;

import com.example.stockservice.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "stock-service", path = "/v1/stock")
public interface StockServiceClient {
//    @GetMapping("/cart/user")
//    ResponseEntity<CartDto> getCart(@RequestHeader("Authorization") String token);
}
