package com.example.E_Commerce.repository;

import com.example.E_Commerce.model.Category;
import com.example.E_Commerce.model.Order;
import com.example.E_Commerce.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUserId(Integer userId);

    @Query(value = "SELECT p " +
            "FROM OrderItem oi JOIN Product p ON oi.productId = p.id " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(oi.productId) DESC")
    List<Product> findTopOrderedProducts(Pageable pageable);


    @Query(value = "SELECT c " +
            "FROM OrderItem oi JOIN Product p ON oi.productId = p.id " +
            "JOIN Category c ON p.category = c.name " +
            "GROUP BY c.id " +
            "ORDER BY COUNT(p.category) DESC")
    List<Category> findTopOrderedCategories(Pageable pageable);


}
