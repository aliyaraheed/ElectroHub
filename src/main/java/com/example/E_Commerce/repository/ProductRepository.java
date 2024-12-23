package com.example.E_Commerce.repository;

import com.example.E_Commerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByIsActiveTrue();

    List<Product> findByCategory(String category);

    List<Product> findByCategoryAndIdNot(String category, Integer id);

    Page<Product> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Product> findByCategoryAndTitleContainingIgnoreCase(String category, String title, Pageable pageable);


}

