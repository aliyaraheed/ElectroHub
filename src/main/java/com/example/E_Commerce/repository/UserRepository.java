package com.example.E_Commerce.repository;

import com.example.E_Commerce.model.UserDtls;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserDtls,Integer> {
    UserDtls findByEmail(String email);

    Boolean existsByEmail(String email);
}
