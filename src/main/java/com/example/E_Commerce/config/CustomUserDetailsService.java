package com.example.E_Commerce.config;

import com.example.E_Commerce.exceptions.UserDisabledException;
import com.example.E_Commerce.model.UserDtls;
import com.example.E_Commerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException, UserDisabledException {
        UserDtls user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        if (!user.getActive()) {
            throw new UserDisabledException("User is disabled: " + email);
        }

        return new CustomUserDetails(user);
    }
}
