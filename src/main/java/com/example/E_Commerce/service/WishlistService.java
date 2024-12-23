package com.example.E_Commerce.service;

import com.example.E_Commerce.model.Product;
import com.example.E_Commerce.model.UserDtls;
import com.example.E_Commerce.model.Wishlist;

import java.util.List;

public interface WishlistService {

   Wishlist getWishlistByUser(UserDtls user);

    Wishlist addProductToWishlist(UserDtls user, Product product);

    Wishlist removeProductFromWishlist(UserDtls user, Product product);

    List<Product> getWishlistProducts(UserDtls user);
}
