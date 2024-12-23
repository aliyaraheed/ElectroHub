package com.example.E_Commerce.service;

import com.example.E_Commerce.dto.ProductDTO;
import com.example.E_Commerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    public Product saveProduct(Product Product);

    public List<Product> getAllProduct();

    public Boolean deleteProduct(Integer id);

    public Product getProductById(Integer id);

//    public Product updateProduct(Product product, MultipartFile file);

    public List<Product> getAllActiveProducts(String category);


    ProductDTO getProductDtoById(Integer id);

    boolean isExistingCategory(String name);

    void updateStock(Integer productId, int quantity);
    
    List<Product> getAllProducts();

    List<Product> getRelatedProductsByCategory(Integer productId, String category);


    Page<Product> searchProductsByCategoryAndName(String category, String search, Pageable pageable);
}
