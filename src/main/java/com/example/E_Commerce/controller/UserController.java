package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.ProductDTO;
import com.example.E_Commerce.dto.request.AddressRequest;
import com.example.E_Commerce.model.*;
import com.example.E_Commerce.service.*;
import com.example.E_Commerce.service.other.CurrentUserDetailProvider;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {
    private final ProductService productService;

    private final CurrentUserDetailProvider currentUserDetailProvider;

    public UserController(ProductService productService, CurrentUserDetailProvider currentUserDetailProvider, OrderService orderService) {
        this.productService = productService;
        this.currentUserDetailProvider = currentUserDetailProvider;
        this.orderService = orderService;
    }

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    private final OrderService orderService;



   

    @GetMapping("/product/{id}")
    public String productDetailPage(@PathVariable("id") Integer id, Model model) {
        ProductDTO product = productService.getProductDtoById(id);
        if (product == null){
            model.addAttribute("error", "Error getting product details. Try again!");
        } else {
            model.addAttribute("product", product);
        }

//        log.info("Taken product category:  {}", product.getCategory());

        List<Product> relatedProducts = productService.getRelatedProductsByCategory(id, product.getCategory());


//        relatedProducts.forEach(rp -> log.info("Taken related products with Id , {} and name {}", rp.getId(), rp.getTitle()));

        model.addAttribute("product", product);

        if (!relatedProducts.isEmpty()){
            model.addAttribute("relatedProducts", relatedProducts);
        }

        return "view_product";
    }


    @GetMapping("/profile")
    public String profilePage(Principal principal, Model model){
        Integer userId = currentUserDetailProvider.getUserId(principal);
        Optional<UserDtls> optionalUserDtls = userService.findUserById(userId);
        if(!optionalUserDtls.isPresent()){
            return "/login";
        }
        UserDtls user = optionalUserDtls.get();
        List<Address> addresses = userService.findAddressByUserId(userId);
        List<Order> orders = orderService.getOrdersByUserId(userId);

        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        model.addAttribute("orders", orders);
        model.addAttribute("addressRequest", new AddressRequest());
        return "user-profile";
    }

    @PostMapping("/addAddress")
    public String addAddress(Principal principal,@ModelAttribute("addressRequest") AddressRequest addressRequest, RedirectAttributes attributes){
        Integer userId = currentUserDetailProvider.getUserId(principal);
        try{
            userService.addAddress(addressRequest, userId);
            attributes.addFlashAttribute("message","Address adding successfully.");
        } catch (Exception e){
          attributes.addFlashAttribute("errorMsg","Address adding Failed. Please tray again!");
        }
        return "redirect:/user/profile";
    }


    @PostMapping("/updateAddress")
    public String updateAddress(@RequestParam("id") Integer addressId,
                                @RequestParam("address") String address,
                                @RequestParam("city") String city,
                                @RequestParam("state") String state,
                                @RequestParam("pincode") String pincode,
                                Principal principal, RedirectAttributes attributes) {
        Integer userId = currentUserDetailProvider.getUserId(principal);
        try {

            userService.updateAddress(addressId, address, city, state, pincode, userId);
            attributes.addFlashAttribute("message", "Address updated successfully.");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMsg", "Address update failed. Please try again!");
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/updateProfile")
    public String updateUserProfile(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("mobileNumber") String mobileNumber,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            Principal principal, RedirectAttributes attributes) {

        Integer userId = currentUserDetailProvider.getUserId(principal);

        try {
            userService.updateUserProfile(userId, name, email, mobileNumber, profileImage);
            attributes.addFlashAttribute("message", "Profile updated successfully.");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMsg", "Profile update failed. Please try again!");
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/orders")
    public String getUserOrders(Principal principal, Model model){
        Integer userId = currentUserDetailProvider.getUserId(principal);
        List<Order> orders = orderService.getOrdersByUserId(userId);

        model.addAttribute("orders", orders);
        return "user-orders";
    }




}
