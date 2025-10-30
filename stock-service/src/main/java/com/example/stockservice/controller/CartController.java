package com.example.stockservice.controller;

import com.example.stockservice.dto.CartDto;
import com.example.stockservice.dto.CartItemDto;
import com.example.stockservice.dto.ProductDto;
import com.example.stockservice.dto.SizeDto;
import com.example.stockservice.jwt.JwtUtil;
import com.example.stockservice.model.Cart;
import com.example.stockservice.model.CartItem;
import com.example.stockservice.request.RemoveCartItemByUserIdRequest;
import com.example.stockservice.request.cart.AddCartItemRequest;
import com.example.stockservice.request.cart.RemoveCartItemRequest;
import com.example.stockservice.request.cart.UpdateCartItemRequest;
import com.example.stockservice.service.cart.CartItemService;
import com.example.stockservice.service.cart.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/stock/cart")
public class CartController {
   private final CartService cartService;
   private final CartItemService cartItemService;
   private final ModelMapper modelMapper;
   private final JwtUtil jwtUtil;

   @PostMapping("/item/add")
   ResponseEntity<CartItemDto> addToCart(@RequestBody AddCartItemRequest request, HttpServletRequest httpRequest){
       String userId = jwtUtil.ExtractUserId(httpRequest);
       CartItem cartItem = cartItemService.addCartItem(request,userId);
       CartItemDto cartItemDto = mapToDto(cartItem);
       return ResponseEntity.ok(cartItemDto);
   }

   @PutMapping("/item/update")
   ResponseEntity<?> updateCartItem(@RequestBody UpdateCartItemRequest request, HttpServletRequest httpRequest){
       try {
           String userId = jwtUtil.ExtractUserId(httpRequest);
           request.setUserId(userId);
           CartItem cartItem = cartItemService.updateCartItem(request);
           CartItemDto cartItemDto = mapToDto(cartItem);
           return ResponseEntity.ok(cartItemDto);
       } catch (Exception e) {
           e.printStackTrace();
           return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
       }
   }

   @DeleteMapping("/item/remove/{cartItemId}")
   ResponseEntity<Void> removeCartItem(@PathVariable String cartItemId, HttpServletRequest httpRequest){
       String userId = jwtUtil.ExtractUserId(httpRequest);
       cartItemService.removeCartItemByCartItemId(userId, cartItemId);
       return ResponseEntity.ok().build();
   }
   
   @DeleteMapping("/item/remove/legacy/{productId}")
   ResponseEntity<Void> removeCartItemLegacy(@PathVariable String productId, HttpServletRequest httpRequest){
       String userId = jwtUtil.ExtractUserId(httpRequest);
       cartItemService.removeCartItem(userId, productId, null);
       return ResponseEntity.ok().build();
   }
   
   @DeleteMapping("/item/remove/legacy/{productId}/{sizeId}")
   ResponseEntity<Void> removeCartItemWithSize(@PathVariable String productId, @PathVariable(required = false) String sizeId, HttpServletRequest httpRequest){
       String userId = jwtUtil.ExtractUserId(httpRequest);
       cartItemService.removeCartItem(userId, productId, sizeId);
       return ResponseEntity.ok().build();
   }

   @GetMapping("/user")
   public ResponseEntity<CartDto> getCart(HttpServletRequest request) {
       return getCartDtoResponseEntity(request);
   }

   private void GetCartDto(Cart cart, CartDto cartDto) {
        cartDto.setId(cart.getId());
        cartDto.setUserId(cart.getUserId());
        cartDto.setTotalAmount(cart.getTotalAmount());

        if (cart.getItems() != null) {
            cartDto.setItems(cart.getItems().stream()
                    .map(this::mapToDto)
                    .collect(java.util.stream.Collectors.toList()));
        } else {
            cartDto.setItems(Collections.emptyList());
        }
   }

   @DeleteMapping("/clear/{cartId}")
   ResponseEntity<Void> clearCartByCartId(@PathVariable String cartId){
       cartService.clearCartByCartId(cartId);
       return ResponseEntity.ok().build();
   }

   @PostMapping("/removeItems")
   ResponseEntity<Void> removeCartItems(@RequestBody RemoveCartItemRequest request){
       cartService.removeCartItemsAndUpdateCart(request.getCartId(), request.getProductIds());
       return ResponseEntity.ok().build();
   }

   @PostMapping("/removeItemsByUserId")
   ResponseEntity<Void> removeCartItemsByUserId(@RequestBody RemoveCartItemByUserIdRequest request){
       cartItemService.removeCartItem(request.getUserId(), request.getProductId(), request.getSizeId());
       return ResponseEntity.ok().build();
   }

   @GetMapping("/getCartByUserId")
   ResponseEntity<CartDto> getCartByUserId(HttpServletRequest request){
       return getCartDtoResponseEntity(request);
   }

    private ResponseEntity<CartDto> getCartDtoResponseEntity(HttpServletRequest request) {
        String userId = jwtUtil.ExtractUserId(request);
        Cart cart = cartService.getCartByUserId(userId);

        CartDto cartDto = new CartDto();

        if (cart == null) {
            cartDto.setId(null);
            cartDto.setUserId(userId);
            cartDto.setTotalAmount(0.0);
            cartDto.setItems(Collections.emptyList());

            return ResponseEntity.ok(cartDto);
        }

        GetCartDto(cart, cartDto);

        return ResponseEntity.ok(cartDto);
    }

    private CartItemDto mapToDto(CartItem cartItem) {
       CartItemDto dto = modelMapper.map(cartItem, CartItemDto.class);
       
       if (cartItem.getId() != null) {
           dto.setId(cartItem.getId());
       }
       
      if (cartItem.getProduct() != null) {
          dto.setProductId(cartItem.getProduct().getId());
          dto.setProductName(cartItem.getProduct().getName());
          dto.setDescription(cartItem.getProduct().getDescription());
          dto.setImageId(cartItem.getProduct().getImageId());
          
          ProductDto productDto = modelMapper.map(cartItem.getProduct(), ProductDto.class);
          dto.setProduct(productDto);
      }
       
       if (cartItem.getSize() != null) {
           dto.setSizeId(cartItem.getSize().getId());
           dto.setSizeName(cartItem.getSize().getName());
           
           SizeDto sizeDto = modelMapper.map(cartItem.getSize(), SizeDto.class);
           dto.setSize(sizeDto);
       }
       return dto;
   }
}
