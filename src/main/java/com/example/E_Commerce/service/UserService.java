package com.example.E_Commerce.service;

import com.example.E_Commerce.dto.CategoryDTO;
import com.example.E_Commerce.dto.ProductDTO;
import com.example.E_Commerce.dto.UserDTO;
import com.example.E_Commerce.dto.request.AddressRequest;
import com.example.E_Commerce.model.Address;
import com.example.E_Commerce.model.UserDtls;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserService {

    public UserDtls saveUser(UserDtls user);

    List<UserDTO> getAllUsers();

    void blockUserOrUnblock(Integer userId);

    List<CategoryDTO> getAllCategoriesDTO();

    List<ProductDTO> getAllProductsDTO();

    boolean userExisting(String email);

    Integer findUserIdByEmail(String email);

    Optional<UserDtls> findUserById(Integer userId);

    void addAddress(AddressRequest addressRequest, Integer userId);

    List<Address> findAddressByUserId(Integer userId);

    void updateAddress(Integer addressId, String address, String city, String state, String pincode, Integer userId);

    void updateUserProfile(Integer userId, String name, String email, String mobileNumber, MultipartFile profileImage);

    void changePassword(String email, String newPass);
}
