package com.example.E_Commerce.service.impl;

import com.example.E_Commerce.model.Product;
import com.example.E_Commerce.model.UserDtls;
import com.example.E_Commerce.model.Wishlist;
import com.example.E_Commerce.repository.WishlistRepository;
import com.example.E_Commerce.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Override
    public Wishlist getWishlistByUser(UserDtls user) {
        return wishlistRepository.findByUser(user).orElse(new Wishlist(user, new ArrayList<>()));

    }

    @Override
    public Wishlist addProductToWishlist(UserDtls user, Product product) {
        Wishlist wishlist = getWishlistByUser(user);
        List<Product> products = wishlist.getProducts();
        if (!products.contains(product)) {
            products.add(product);
        }
        wishlist.setProducts(products);
        return wishlistRepository.save(wishlist);
    }

    @Override
    public Wishlist removeProductFromWishlist(UserDtls user, Product product) {
        Wishlist wishlist = getWishlistByUser(user);
        List<Product> products = wishlist.getProducts();
        products.remove(product);
        wishlist.setProducts(products);
        return wishlistRepository.save(wishlist);
    }

    @Override
    public List<Product> getWishlistProducts(UserDtls user) {
        return getWishlistByUser(user).getProducts();
    }
}

