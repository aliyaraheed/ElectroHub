package com.example.E_Commerce.service.impl;

import com.example.E_Commerce.dto.CategoryDTO;
import com.example.E_Commerce.dto.ProductDTO;
import com.example.E_Commerce.dto.UserDTO;
import com.example.E_Commerce.dto.request.AddressRequest;
import com.example.E_Commerce.model.*;
import com.example.E_Commerce.repository.AddressRepository;
import com.example.E_Commerce.repository.CategoryRepository;
import com.example.E_Commerce.repository.ProductRepository;
import com.example.E_Commerce.repository.UserRepository;
import com.example.E_Commerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final SessionRegistry sessionRegistry;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;


    public UserServiceImpl(PasswordEncoder passwordEncoder, SessionRegistry sessionRegistry, CategoryRepository categoryRepository, ProductRepository productRepository, AddressRepository addressRepository) {
        this.passwordEncoder = passwordEncoder;
        this.sessionRegistry = sessionRegistry;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
    }


    @Override
    public UserDtls saveUser(UserDtls user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Roles.USER);
        user.setActive(true);
        UserDtls saveUser = userRepository.save(user);
        return saveUser;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<UserDtls> userDtlsList = userRepository.findAll();

        return convertToDto(userDtlsList);
    }

    @Override
    public void blockUserOrUnblock(Integer userId) {
        UserDtls user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        if (user.getActive()) {
            String username = user.getEmail();

            for (Object principal : sessionRegistry.getAllPrincipals()) {
                if (principal instanceof org.springframework.security.core.userdetails.User) {
                    org.springframework.security.core.userdetails.User springUser =
                            (org.springframework.security.core.userdetails.User) principal;

                    if (springUser.getUsername().equals(username)) {
                        for (SessionInformation sessionInfo : sessionRegistry.getAllSessions(principal, false)) {
                            sessionInfo.expireNow();
                        }
                    }
                }
            }
        }

        user.setActive(!user.getActive());

        userRepository.save(user);
    }

    @Override
    public List<CategoryDTO> getAllCategoriesDTO() {

        List<Category> categories = categoryRepository.findByIsActiveTrue();

        List<CategoryDTO> categoryDTOs = categories.stream().map(category -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(category.getId());
            dto.setName(category.getName());
            dto.setImageName(category.getImageName());
            return dto;
        }).toList();

        return categoryDTOs;
    }

    @Override
    public List<ProductDTO> getAllProductsDTO() {

        int limit = 8;
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = new ArrayList<>(productRepository.findAll(pageable).getContent());

        List<ProductDTO> productDTOs = products.stream().map(product -> {
            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setTitle(product.getTitle());
            dto.setDescription(product.getDescription());
            dto.setCategory(product.getCategory());
            dto.setPrice(product.getPrice());
            dto.setStock(product.getStock());
            dto.setImages(product.getImages());
            dto.setDiscount(product.getDiscount());
            dto.setDiscountPrice(product.getDiscountPrice());
            return dto;
        }).toList();

        return productDTOs;
    }

    @Override
    public boolean userExisting(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Integer findUserIdByEmail(String email) {
        UserDtls user = userRepository.findByEmail(email);
        return user.getId();
    }

    @Override
    public Optional<UserDtls> findUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    @Override
    public void addAddress(AddressRequest addressRequest, Integer userId) {

        UserDtls user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Address address = new Address();
        address.setAddress(addressRequest.getAddress());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setPincode(addressRequest.getPincode());
        address.setPhone(addressRequest.getPhone());
        address.setUser(user);

        addressRepository.save(address);
    }

    @Override
    public List<Address> findAddressByUserId(Integer userId) {
        return Optional.ofNullable(addressRepository.findByUserId(userId)).orElse(List.of());
    }

    @Override
    public void updateAddress(Integer addressId, String address, String city, String state, String pincode, Integer userId) {
        UserDtls user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Address existingAddress = addressRepository.findById(Long.valueOf(addressId))
                .orElseThrow(() -> new IllegalArgumentException("Address not found with ID: " + addressId));
        if (!existingAddress.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to the user.");
        }

        existingAddress.setAddress(address);
        existingAddress.setCity(city);
        existingAddress.setState(state);
        existingAddress.setPincode(pincode);

        addressRepository.save(existingAddress);
    }


    private List<UserDTO> convertToDto(List<UserDtls> userDtlsList) {
        List<UserDTO> userDTOList = new ArrayList<>();

        for (UserDtls userDtls : userDtlsList) {
            if (userDtls.getRole().equals(Roles.ADMIN)) {
                continue;
            }
            UserDTO userDTO = new UserDTO();
            userDTO.setId(userDtls.getId());
            userDTO.setEmail(userDtls.getEmail());
            userDTO.setProfileImage(userDtls.getProfileImage());
            userDTO.setIsActive(userDtls.getActive());
            userDTO.setRole(userDtls.getRole().name());
            userDTOList.add(userDTO);
        }
        return userDTOList;
    }

    @Override
    public void updateUserProfile(Integer userId, String name, String email, String mobileNumber, MultipartFile profileImage) {
        UserDtls user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(name);
        user.setEmail(email);
        user.setMobileNumber(mobileNumber);

        if (!profileImage.isEmpty()) {

            String uploadDir = "src/main/resources/Static/img/profile_images/";
            Path uploadPath = Paths.get(uploadDir);


            try {
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }


                String originalFilename = profileImage.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = "user_" + userId + "_" + System.currentTimeMillis() + fileExtension;

                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.copy(profileImage.getInputStream(), filePath);

                user.setProfileImage(uniqueFilename);

            } catch (IOException e) {
                throw new RuntimeException("Failed to save profile image", e);
            }
        }

        userRepository.save(user);
    }

    @Override
    public void changePassword(String email, String newPass) {
        UserDtls userDtls = userRepository.findByEmail(email);
        userDtls.setPassword(passwordEncoder.encode(newPass));
        userRepository.save(userDtls);
    }


}





