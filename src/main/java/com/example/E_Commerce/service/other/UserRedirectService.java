package com.example.E_Commerce.service.other;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Set;

@Service
public class UserRedirectService {

    public String redirectUserByRole(Principal principal,@AuthenticationPrincipal OAuth2User user) {
        if (principal != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            if (roles.contains("ADMIN")) {
                return "redirect:/admin/index";
            } else if (roles.contains("USER")) {
                return "redirect:/";
            }

        }

        if (user != null && user.getAttribute("email") != null) {
            return "redirect:/";
        }

        return null;
    }
}