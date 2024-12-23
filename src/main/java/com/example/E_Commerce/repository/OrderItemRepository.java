package com.example.E_Commerce.repository;

import com.example.E_Commerce.model.Order;
import com.example.E_Commerce.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi.productCategory, COUNT(oi) as purchaseCount FROM OrderItem oi GROUP BY oi.productCategory ORDER BY purchaseCount DESC")
    List<Object[]> findTopPurchasedCategoriesWithRank();

    @Query("SELECT oi.productTitle, COUNT(oi) as purchaseCount FROM OrderItem oi GROUP BY oi.productTitle ORDER BY purchaseCount DESC")
    List<Object[]> findTopPurchasedProductsWithRank();

}

