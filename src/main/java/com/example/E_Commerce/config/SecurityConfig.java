package com.example.E_Commerce.config;

import com.example.E_Commerce.exceptions.UserDisabledException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SuccessHandler successHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(SuccessHandler successHandler, CustomOAuth2UserService customOAuth2UserService) {
        this.successHandler = successHandler;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                                .requestMatchers("/user/**","/wishlist/**","/coupons/**").hasAnyAuthority("USER")
                                .requestMatchers("/**", "/login", "/signup", "/assets", "/product/{productId}", "/product/", "/saveUser", "/quantity-increment-and-decrement/**", "/paymentVerifyAndOrder").permitAll()
                                .anyRequest().authenticated()
)
                .formLogin(form ->
                        form
                                .loginPage("/login").permitAll()
                                .successHandler(successHandler)
                                .failureHandler((request, response, exception) -> {
                                    if (exception instanceof AuthenticationException) {
                                        AuthenticationException authException = (AuthenticationException) exception;
                                        if (authException.getCause() instanceof UserDisabledException) {
                                            request.getSession().setAttribute("loginError", "Your account is disabled. Please contact support.");
                                        } else {
                                            request.getSession().setAttribute("loginError", "Invalid Email or Password");
                                        }
                                    }
                                    response.sendRedirect("/login");
                                })
                ).logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                                .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)
                ))
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .sessionRegistry(sessionRegistry())
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

}
