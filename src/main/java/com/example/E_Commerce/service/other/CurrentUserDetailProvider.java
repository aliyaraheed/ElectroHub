package com.example.E_Commerce.service.other;

import com.example.E_Commerce.model.UserDtls;
import com.example.E_Commerce.repository.UserRepository;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Service
public class CurrentUserDetailProvider {

    private final UserRepository userRepository;

    public CurrentUserDetailProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getCurrentUserEmail(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Principal cannot be null");
        }
        return principal.getName();
    }

    public Integer getUserId(Principal principal){
        UserDtls userInfo = userRepository.findByEmail(getCurrentUserEmail(principal));
       return userInfo.getId();
    }

    public UserDtls getUserDtls(Principal principal){
        UserDtls userInfo = userRepository.findByEmail(getCurrentUserEmail(principal));
        return userInfo;
    }

}
