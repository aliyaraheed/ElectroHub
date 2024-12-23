package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.CategoryDTO;
import com.example.E_Commerce.dto.ProductDTO;
import com.example.E_Commerce.dto.UserDTO;
import com.example.E_Commerce.model.Category;
import com.example.E_Commerce.model.Product;
import com.example.E_Commerce.model.UserDtls;
import com.example.E_Commerce.service.CartService;
import com.example.E_Commerce.service.CategoryService;
import com.example.E_Commerce.service.ProductService;
import com.example.E_Commerce.service.UserService;
import com.example.E_Commerce.service.other.UserRedirectService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.stream.Collectors;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    private final UserRedirectService redirectService;

    public HomeController(UserRedirectService redirectService) {
        this.redirectService = redirectService;
    }

    @GetMapping("/")
    public String index(Model model){
       List<CategoryDTO> categoryDTOS = userService.getAllCategoriesDTO();
       List<ProductDTO> productDTOS = userService.getAllProductsDTO();
       model.addAttribute("categories", categoryDTOS);
       model.addAttribute("products", productDTOS);
       return "index";
    }

    @GetMapping("/login")
    public String login(Principal principal, @AuthenticationPrincipal OAuth2User user){
       String redirect = redirectService.redirectUserByRole(principal, user);
       if (redirect != null){
           return redirect;
       }
        return "login";
    }

    @GetMapping("/register")
    public String register(Principal principal, @AuthenticationPrincipal OAuth2User user){
        String redirect = redirectService.redirectUserByRole(principal, user);
        if (redirect != null){
            return redirect;
        }
        return "register";
    }

    @GetMapping("/base")
    public String base(){
        return "base";
    }

    @GetMapping("/products")
    public String products(Model m,
                           @RequestParam(value = "category", defaultValue = "") String category,
                           @RequestParam(value = "search", defaultValue = "") String search,
                           @RequestParam(name = "priceSort", required = false) Boolean priceSort,
                           @RequestParam(name = "letterSort", required = false) Boolean letterSort,
                           @RequestParam(name = "page", defaultValue = "0", required = false) int page,
                           @RequestParam(name = "size", defaultValue = "8", required = false) int size) {

        List<Category> categories = categoryService.getAllActiveCategory();

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.searchProductsByCategoryAndName(category, search, pageable);

        List<Product> products = new ArrayList<>(productPage.getContent());

        if (priceSort != null) {
            if (priceSort) {
                products.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
            } else {
                products.sort((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
            }
        }

        if (letterSort != null) {
            if (letterSort) {
                products.sort((p1, p2) -> p1.getTitle().compareToIgnoreCase(p2.getTitle()));
            } else {
                products.sort((p1, p2) -> p2.getTitle().compareToIgnoreCase(p1.getTitle()));
            }
        }

        m.addAttribute("categories", categories);
        m.addAttribute("products", products);
        m.addAttribute("paramValue", category);
        m.addAttribute("search", search);
        m.addAttribute("priceSort", priceSort);
        m.addAttribute("letterSort", letterSort);
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", productPage.getTotalPages());
        m.addAttribute("size", size);
        m.addAttribute("totalProducts", productPage.getTotalElements());

        return "product";
    }


}
