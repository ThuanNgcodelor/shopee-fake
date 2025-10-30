package com.example.stockservice.repository;

import com.example.stockservice.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    CartItem findByCartIdAndProductId(String cartId, String productId);
    void deleteByCartIdAndProductIdIn(String id, List<String> productIds);
    void deleteAllByCartId(String id);

    Optional<CartItem> findByCart_IdAndProduct_Id(String cartId, String productId);

    void deleteByCart_IdAndProduct_IdIn(String cartId, List<String> productIds);

    void deleteAllByCart_Id(String cartId);

    List<CartItem> findAllByCart_Id(String cartId);
}
