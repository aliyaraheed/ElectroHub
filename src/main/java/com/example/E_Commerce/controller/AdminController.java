package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.UserDTO;
import com.example.E_Commerce.model.Category;
import com.example.E_Commerce.model.Product;
import com.example.E_Commerce.service.CategoryService;
import com.example.E_Commerce.service.ProductService;
import com.example.E_Commerce.service.UserService;
import com.example.E_Commerce.service.impl.CloudinaryService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    private final CloudinaryService cloudinaryService;

    private final UserService userService;

    public AdminController(CloudinaryService cloudinaryService, UserService userService) {
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
    }

    @GetMapping("/index")
    public String index(){
        return "admin/index";
    }


    @GetMapping("/category")
    public String category(Model m){
        m.addAttribute("categorys",categoryService.getAllCategory());
        return "admin/category";
    }

    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
                               HttpSession session, RedirectAttributes redirectAttributes) throws IOException {
        if(productService.isExistingCategory(category.getName())){
            redirectAttributes.addFlashAttribute("categoryError", "Category is already existing!");
            return "redirect:/admin/category";
        }

        String imageName = cloudinaryService.uploadAndGetCloudinaryUrl(file);
        category.setImageName(imageName);


        Boolean existCategory = categoryService.existCategory(category.getName());


        if (existCategory){
            session.setAttribute("errorMsg","category Name already exists");
        }else {
            Category saveCategory = categoryService.saveCategory(category);

            if(ObjectUtils.isEmpty(saveCategory)){
                session.setAttribute("errorMsg", "Not saved! internal server error!");
            }else {

                session.setAttribute("successMsg","saved successfully");
            }
        }
        return "redirect:/admin/category";

    }

    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session){

        Boolean deleteCategory = categoryService.deleteCategory(id);

        if(deleteCategory){
            session.setAttribute("successMsg","category deleted");
        }else{
            session.setAttribute("errorMsg","something went wrong");
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/loadEditCategory/{id}")
    public String loadEditCategory(@PathVariable  int id, Model m){
        m.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/edit_category";
    }

    @PostMapping("/updateCategory")
    public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {

        Category oldcategory =  categoryService.getCategoryById(category.getId());
        String imageName =  cloudinaryService.uploadAndGetCloudinaryUrl(file);

        if (!ObjectUtils.isEmpty(category)){
            oldcategory.setName(category.getName());
            oldcategory.setIsActive(category.getIsActive());
            oldcategory.setImageName(imageName);
        }

        Category updateCategory = categoryService.saveCategory(oldcategory);

        if (!ObjectUtils.isEmpty(updateCategory)){

            session.setAttribute("successMsg","category update success");
        }else {
            session.setAttribute("errorMsg","something wrong on server");
        }
       return "redirect:/admin/category";
    }

//product
    @GetMapping("/loadAddProduct")
    public String loadAddProduct(Model m){
        List<Category> categories = categoryService.getAllCategory();
        m.addAttribute("categories",categories);
        return "admin/add_product";
    }

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam("file1") MultipartFile file1,
                              @RequestParam("file2") MultipartFile file2,
                              @RequestParam("file3") MultipartFile file3,
                              HttpSession session) {


        if (file1.isEmpty() || file2.isEmpty() || file3.isEmpty()) {
            session.setAttribute("errorMsg", "Please upload exactly 3 images.");
            return "redirect:/admin/loadAddProduct";
        }


        List<String> imageNames = new ArrayList<>();


        MultipartFile[] files = {file1, file2, file3};

        for (MultipartFile file : files) {
            try {
                String imageUrl = cloudinaryService.uploadAndGetCloudinaryUrl(file);
                imageNames.add(imageUrl);
            } catch (Exception e) {
                session.setAttribute("errorMsg", "Failed to upload images. Please try again.");
                return "redirect:/admin/loadAddProduct";
            }
        }

        product.setImages(imageNames);

        session.setAttribute("successMsg", "Product and images saved successfully.");


        Product savedProduct = productService.saveProduct(product);

        for (String url : imageNames){
            log.info("imageUrl : {}", url);
        }

        return "redirect:/admin/products";
    }



    @GetMapping("/products")
    public String loadViewProduct(Model m){
        m.addAttribute("products",productService.getAllProduct());
        return "admin/products";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id, HttpSession session){
     Boolean deleteProduct =   productService.deleteProduct(id);

     if (deleteProduct){
         session.setAttribute("successMsg", "product delete success");
     }else {
         session.setAttribute("errorMsg","something wrong on server");
     }
        return "redirect:/admin/products";
    }

    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable int id, Model m){
        m.addAttribute("product",productService.getProductById(id));
        m.addAttribute("categories",categoryService.getAllCategory());
        return "admin/edit_product";
    }

    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute Product product,
                                @RequestParam("imageFiles") MultipartFile[] images,
                                HttpSession session, Model m) {


        if (product.getDiscount() < 0 || product.getDiscount() > 100) {
            session.setAttribute("errorMsg", "Invalid Discount");
            return "redirect:/admin/editProduct/" + product.getId();
        }


        if (images.length != 3) {
            session.setAttribute("errorMsg", "Please upload exactly 3 images.");
            return "redirect:/admin/editProduct/" + product.getId();
        }


        Product existingProduct = productService.getProductById(product.getId());
        if (existingProduct == null) {
            session.setAttribute("errorMsg", "Product not found.");
            return "redirect:/admin/products";
        }


        existingProduct.setTitle(product.getTitle());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDiscount(product.getDiscount());
        existingProduct.setDiscountPrice(product.getDiscountPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.setCategory(product.getCategory());

        List<String> updatedImageNames = new ArrayList<>();


        try {

            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String imageName = cloudinaryService.uploadAndGetCloudinaryUrl(image);
                    updatedImageNames.add("/img/product/" + imageName);
                } else {

                    updatedImageNames.add(existingProduct.getImages().get(updatedImageNames.size()));
                }
            }

        } catch (IOException e) {
            session.setAttribute("errorMsg", "Image upload failed: " + e.getMessage());
            return "redirect:/admin/editProduct/" + product.getId();
        }

        existingProduct.setImages(updatedImageNames);

        Product updatedProduct = productService.saveProduct(existingProduct);

        if (updatedProduct != null) {
            session.setAttribute("successMsg", "Product updated successfully.");
            m.addAttribute("product", updatedProduct);
        } else {
            session.setAttribute("errorMsg", "Failed to update the product.");
        }

        return "redirect:/admin/editProduct/" + product.getId();
    }


    @GetMapping("/users")
    public String getAllUsersPage(Model model) {
        List<UserDTO> userDTOs = new ArrayList<>();
        try {
            userDTOs = userService.getAllUsers();
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while fetching users.");
        }
        model.addAttribute("users", userDTOs);
        return "admin/user-list";
    }


    @PostMapping("/block-unblock-user/{id}")
    public String blockUserOrUnblock(@PathVariable("id") Integer userId){
      userService.blockUserOrUnblock(userId);
      return "redirect:/admin/users";
    }



}

