package com.example.E_Commerce.service;

import com.example.E_Commerce.model.Cart;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface CartService {

    void addToCart(Integer productId, Integer userId);

    Optional<Cart> getCartByUserId(Integer userId);

    void removeCartItem(Long itemId, Integer userId);

    ResponseEntity<?> cartItemQuantityUpdate(Integer userId, Long itemId, boolean increment, boolean decrement);
}
