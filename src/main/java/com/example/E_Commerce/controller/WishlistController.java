package com.example.E_Commerce.controller;

import com.example.E_Commerce.model.Product;
import com.example.E_Commerce.model.UserDtls;
import com.example.E_Commerce.service.ProductService;
import com.example.E_Commerce.service.WishlistService;
import com.example.E_Commerce.service.other.CurrentUserDetailProvider;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/wishlist")
@Slf4j
public class WishlistController {

    private static final Logger log = LoggerFactory.getLogger(WishlistController.class);
    @Autowired
    private final WishlistService wishlistService;

    private final ProductService productService;

    private final CurrentUserDetailProvider currentUserDetailProvider;

    public WishlistController(WishlistService wishlistService, ProductService productService, CurrentUserDetailProvider currentUserDetailProvider) {
        this.wishlistService = wishlistService;
        this.productService = productService;
        this.currentUserDetailProvider = currentUserDetailProvider;
    }


    @PostMapping("/add/{productId}")
    public String addProductToWishlist(Principal principal,@PathVariable Integer productId) {
        log.info("adding to wishlist product Id : {}", productId);
        UserDtls user = currentUserDetailProvider.getUserDtls(principal);
        Product product = productService.getProductById(productId);
        wishlistService.addProductToWishlist(user, product);
       return  "redirect:/wishlist";    }

    @PostMapping("/remove/{productId}")
    public String removeProductFromWishlist(Principal principal,@PathVariable Integer productId) {
        UserDtls user = currentUserDetailProvider.getUserDtls(principal);
        Product product = productService.getProductById(productId);
        wishlistService.removeProductFromWishlist(user, product);
        return "redirect:/wishlist";
    }

    @GetMapping
    public String getWishlistProducts(Principal principal, Model model) {
        UserDtls user = currentUserDetailProvider.getUserDtls(principal);
        List<Product> productList = wishlistService.getWishlistProducts(user);
        model.addAttribute("wishlistProducts", productList);
        return "wishlist";
    }
}
