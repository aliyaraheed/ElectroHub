package com.example.E_Commerce.controller;

import com.example.E_Commerce.model.Cart;
import com.example.E_Commerce.model.Coupon;
import com.example.E_Commerce.model.DiscountType;
import com.example.E_Commerce.model.Product;
import com.example.E_Commerce.service.CartService;
import com.example.E_Commerce.service.CouponService;
import com.example.E_Commerce.service.ProductService;
import com.example.E_Commerce.service.UserService;
import com.example.E_Commerce.service.other.CurrentUserDetailProvider;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final CurrentUserDetailProvider currentUserDetailProvider;
    private final CartService cartService;
    private final ProductService productService;
    private final UserService userService;
    private final CouponService couponService;

    public CartController(CurrentUserDetailProvider currentUserDetailProvider, CartService cartService, ProductService productService, UserService userService, CouponService couponService) {
        this.currentUserDetailProvider = currentUserDetailProvider;
        this.cartService = cartService;
        this.productService = productService;
        this.userService = userService;
        this.couponService = couponService;
    }

    @GetMapping("/user/cart")
    public String cartPage(Model model, Principal principal, Authentication authentication){
        Integer userId = currentUserDetailProvider.getUserId(principal);
        Optional<Cart> cart = cartService.getCartByUserId(userId);
          model.addAttribute("authentication", authentication);
        if (cart.isPresent()) {
            model.addAttribute("cart", cart.get());
        } else {
            model.addAttribute("cart", null);
        }

        return "cart";
    }

    @PostMapping("/user/addToCart")
    public String addToCart(@RequestParam("productId") Integer productId, Principal principal, RedirectAttributes redirectAttributes) {
        Integer userId = currentUserDetailProvider.getUserId(principal);
        log.info("Add to cart with prodcut id {} and user id {}", productId,userId );
        try {
            cartService.addToCart(productId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add product to cart. Please try again.");
        }

        return "redirect:/user/cart";
    }


    @PostMapping("/user/removeCartItem")
    public String removeCartItem(@RequestParam("ItemId") Long itemId, Principal principal, RedirectAttributes redirectAttributes) {
        Integer userId = currentUserDetailProvider.getUserId(principal);
        try {
            cartService.removeCartItem(itemId, userId);

            redirectAttributes.addFlashAttribute("message", "Item successfully removed from your cart.");
            return "redirect:/user/cart";
        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/cart";
        }
    }

    @PostMapping("/user/quantity-increment-and-decrement")
    public String cartItemQuantityUpdate(@RequestParam("itemId") Long itemId,
                                                    @RequestParam(name = "increment", required = false) boolean increment,
                                                    @RequestParam(value = "decrement", required = false) boolean decrement,
                                                    Principal principal) {
        Integer userId  = currentUserDetailProvider.getUserId(principal);
        try {
            cartService.cartItemQuantityUpdate(userId, itemId, increment, decrement);
            return "redirect:/user/cart";
        } catch (Exception e) {
            log.error("Error updating cart item quantity", e);
            return "redirect:/user/cart";
        }
    }






}
