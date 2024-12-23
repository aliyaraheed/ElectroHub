package com.example.E_Commerce.controller;

import com.example.E_Commerce.model.UserDtls;
import com.example.E_Commerce.service.UserService;
import com.example.E_Commerce.service.other.MailService;
import com.example.E_Commerce.service.other.UserRedirectService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.model.IModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    private final MailService mailService;

    private final UserRedirectService redirectService;

    private AuthController(UserService userService, MailService mailService, UserRedirectService redirectService) {
        this.userService = userService;
        this.mailService = mailService;
        this.redirectService = redirectService;
    }

    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file,
                           HttpSession session, RedirectAttributes redirectAttributes) throws IOException {
        log.info("User save method called with email: {}", user.getEmail());

        if (userService.userExisting(user.getEmail())) {
            redirectAttributes.addFlashAttribute("emailError", "User already exists with this email!");
            return "redirect:/register";
        }


        String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
        user.setProfileImage(imageName);


        if (!file.isEmpty()) {
            try {
                File saveFile = new ClassPathResource("Static/img").getFile();
                File saveDirectory = new File(saveFile, "profile_img");


                if (!saveDirectory.exists()) {
                    saveDirectory.mkdirs();
                }


                Path path = Paths.get(saveDirectory.getAbsolutePath(), imageName);


                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error("Failed to save profile image: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("errorMsg", "Failed to upload profile image.");
                return "redirect:/register";
            }
        }


        session.setAttribute("tempUser", user);
        mailService.generateAndSendOtp(user.getEmail(), session.getId());

        return "redirect:/verifyOtp";
    }


    @GetMapping("/verifyOtp")
    public String verifyOtpPage(HttpSession session) {
        if (session.getAttribute("tempUser") == null) {
            return "redirect:/login";
        }
        return "verifyOtp";
    }

    @PostMapping("/veryOtp")
    public String veryOtpRequest(@RequestParam("otp") Integer otp, HttpSession session) {
        UserDtls tempUser = (UserDtls) session.getAttribute("tempUser");
//        log.info();
        if (tempUser == null) {
            session.setAttribute("errorMsg", "Session expired. Please register again.");
            return "redirect:/register";
        }

        try {
            mailService.verifyOtp(otp.toString(), session.getId());

            UserDtls savedUser = userService.saveUser(tempUser);
            if (savedUser != null) {
                session.setAttribute("successMsg", "Registered successfully!");
                session.removeAttribute("tempUser");
                return "redirect:/login";
            } else {
                session.setAttribute("errorMsg", "Failed to save user. Please try again.");
                return "redirect:/register";
            }
        } catch (MailService.OtpTimeoutException e) {
            session.setAttribute("errorMsg", e.getMessage());
        } catch (MailService.OtpVerificationException e) {
            log.info("Mail verification issue");
            session.setAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/verifyOtp";
    }


    @GetMapping("/forgetPassword")
    public String forgetPasswordPage(Principal principal, @AuthenticationPrincipal OAuth2User user){
        String redirect = redirectService.redirectUserByRole(principal, user);
        if (redirect != null){
            return redirect;
        }
        return "forgot-password";
    }

    @PostMapping("/forget-password")
    public String forgetPasswordRequest(@RequestParam("email") String email, HttpSession session, RedirectAttributes attributes){
        if(!userService.userExisting(email)){
            attributes.addFlashAttribute("error", "user not found with email");
            return "redirect:/forgetPassword";
        }
        mailService.generateAndSendOtp(email,session.getId());
        attributes.addFlashAttribute("email", email);
        return "redirect:/verify-and-create-password";
    }


    @GetMapping("/verify-and-create-password")
    public String verifyAndCreatePassword(Principal principal, @AuthenticationPrincipal OAuth2User user){
        String redirect = redirectService.redirectUserByRole(principal, user);
        if (redirect != null){
            return redirect;
        }
        return "verify-otp";
    }

    @PostMapping("/verify-and-create-password")
    public String verifyPasswordRequest(
            @RequestParam("pass") String newPass,
            @RequestParam("otp") String otp,
            @RequestParam("email") String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {

            mailService.verifyOtp(otp, session.getId());


            userService.changePassword(email, newPass);


            redirectAttributes.addFlashAttribute("success", "Password has been reset successfully.");


            return "redirect:/login";
        }catch (MailService.OtpTimeoutException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forget-password";
        } catch (MailService.OtpVerificationException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forget-password";
        }
        catch (Exception e) {

            redirectAttributes.addFlashAttribute("error", "Something went wrong. Please try again.");
            return "redirect:/forget-password";
        }
    }




}
