package com.example.E_Commerce.service.impl;

import com.example.E_Commerce.model.Cart;
import com.example.E_Commerce.model.CartItem;
import com.example.E_Commerce.model.Product;
import com.example.E_Commerce.model.UserDtls;
import com.example.E_Commerce.repository.CartItemRepository;
import com.example.E_Commerce.repository.CartRepository;
import com.example.E_Commerce.repository.ProductRepository;
import com.example.E_Commerce.repository.UserRepository;
import com.example.E_Commerce.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void addToCart(Integer productId, Integer userId) {
        UserDtls user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        Cart cart;

        if (!optionalCart.isPresent()) {

            cart = new Cart();
            cart.setUser(user);
            cart.setItems(new ArrayList<>());
            cart.setTotalPrice(0.0);
        } else {

            cart = optionalCart.get();
        }


        List<CartItem> items = cart.getItems();
        Optional<CartItem> existingCartItem = items.stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst();

        if (existingCartItem.isPresent()) {

            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        } else {

            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
            items.add(cartItem);
        }


        double totalPrice = items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        cart.setTotalPrice(totalPrice);


        cartRepository.save(cart);
    }

    @Override
    public Optional<Cart> getCartByUserId(Integer userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    public void removeCartItem(Long itemId, Integer userId) {
        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);

        if (!optionalCart.isPresent()) {
            throw new RuntimeException("Cart not found for user with id: " + userId);
        }

        Cart cart = optionalCart.get();

        CartItem itemToRemove = null;
        for (CartItem item : cart.getItems()) {
            if (item.getId().equals(itemId)) {
                itemToRemove = item;
                break;
            }
        }

        if (itemToRemove == null) {
            throw new RuntimeException("CartItem not found with id: " + itemId);
        }

        cart.getItems().remove(itemToRemove);

        double updatedTotalPrice = 0;
        for (CartItem cartItem : cart.getItems()) {
            updatedTotalPrice += cartItem.getProduct().getPrice() * cartItem.getQuantity();
        }

        cart.setTotalPrice(updatedTotalPrice);

        cartRepository.save(cart);
    }

    @Override
    public ResponseEntity<?> cartItemQuantityUpdate(Integer userId, Long itemId, boolean increment, boolean decrement) {

        Optional<UserDtls> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        UserDtls user = optionalUser.get();

        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        if (optionalCart.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart not found for user");
        }

        Cart cart = optionalCart.get();

        Optional<CartItem> optionalCartItem = cartItemRepository.findById(itemId);
        if (optionalCartItem.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart item not found");
        }

        CartItem cartItem = optionalCartItem.get();

        if (!cart.getItems().contains(cartItem)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Item does not belong to the user's cart");
        }

        Product product =cartItem.getProduct();
        int availableStock = product.getStock();

        if (increment) {
            if (cartItem.getQuantity() < availableStock) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot exceed available stock");
            }
        } else if (decrement) {
            if (cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
            } else {
                cart.getItems().remove(cartItem);
                cartItemRepository.delete(cartItem);
            }
        }

        double totalPrice = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        cart.setTotalPrice(totalPrice);

        cartRepository.save(cart);

        return ResponseEntity.ok("Cart updated successfully");
    }



}
