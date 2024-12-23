package com.example.E_Commerce.config;

import com.example.E_Commerce.model.UserDtls;
import com.example.E_Commerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User;
        try {
            oAuth2User = super.loadUser(userRequest);
        } catch (Exception e) {
            log.error("Error loading user from OAuth2 provider", e);
            throw e;
        }

        log.info("OAuth2 User Attributes: {}", oAuth2User.getAttributes());

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String imageUrl = oAuth2User.getAttribute("picture");

        if (email == null) {
            log.error("Email not found in OAuth2 user attributes");
            throw new RuntimeException("Email is required for registration");
        }

        if (!userRepository.existsByEmail(email)) {
            UserDtls user = new UserDtls();
            user.setName(name != null ? name : "Unknown");
            user.setEmail(email);
            user.setProfileImage(imageUrl);
            userRepository.save(user);
            log.info("New user saved: {}", user);
        } else {
            log.info("User already exists with email: {}", email);
        }

        return oAuth2User;
    }


}
