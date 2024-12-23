package com.example.E_Commerce.service.impl;

import com.example.E_Commerce.dto.ProductDTO;
import com.example.E_Commerce.model.Category;
import com.example.E_Commerce.model.Product;
import com.example.E_Commerce.repository.CategoryRepository;
import com.example.E_Commerce.repository.ProductRepository;
import com.example.E_Commerce.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    @Autowired
    private ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Product saveProduct(Product product) {
        Double discount =  product.getPrice()*(product.getDiscount()/100.0);
        Double discountPrice = product.getPrice() - discount;
        product.setDiscountPrice(discountPrice);
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    @Override
    public Boolean deleteProduct(Integer id) {
      Product product =  productRepository.findById(id).orElse(null);

        if (!ObjectUtils.isEmpty(product)) {
            productRepository.delete(product);
            return true;
        }
        return false;
    }

    @Override
    public Product getProductById(Integer id) {
        Product product = productRepository.findById(id).orElse(null);
        return product;
    }


    @Override
    public List<Product> getAllActiveProducts(String category) {
        List<Product> products = null;
        if (ObjectUtils.isEmpty(category)) {
            products = productRepository.findByIsActiveTrue();
        }else {
            products=productRepository.findByCategory(category);
        }

        return products;
    }

    @Override
    public ProductDTO getProductDtoById(Integer id) {

        Optional<Product> productOptional = productRepository.findById(id);

        if (productOptional.isPresent()) {
            Product product = productOptional.get();

            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getId());
            productDTO.setTitle(product.getTitle());
            productDTO.setDescription(product.getDescription());
            productDTO.setCategory(product.getCategory());
            productDTO.setPrice(product.getPrice());
            productDTO.setImages(product.getImages());
            productDTO.setDiscount(product.getDiscount());
            productDTO.setStock(product.getStock());
            productDTO.setDiscountPrice(product.getDiscountPrice());

            return productDTO;
        } else {
            return null;
        }
    }

    @Override
    public boolean isExistingCategory(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    public void updateStock(Integer productId, int quantity) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        int updatedStock = product.getStock() + quantity;
        product.setStock(updatedStock);
        productRepository.save(product);
    }

    @Override
    public Page<Product> searchProductsByCategoryAndName(String category, String name, Pageable pageable) {
        if (category == null || category.isEmpty()) {
            return productRepository.findByTitleContainingIgnoreCase(name, pageable);
        } else {
            return productRepository.findByCategoryAndTitleContainingIgnoreCase(category, name, pageable);
        }
    }


    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getRelatedProductsByCategory(Integer productId, String category) {
        System.out.println("Related prodcugt reaquest with product Id : "+ productId +  " and category " + category);
        List<Product> relatedProducts = productRepository.findByCategoryAndIdNot(category,productId);
                return relatedProducts;
    }


    }






