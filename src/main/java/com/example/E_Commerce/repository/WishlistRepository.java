package com.example.E_Commerce.repository;

import com.example.E_Commerce.model.UserDtls;
import com.example.E_Commerce.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist,Long> {
    Optional<Wishlist> findByUser(UserDtls user);
}
