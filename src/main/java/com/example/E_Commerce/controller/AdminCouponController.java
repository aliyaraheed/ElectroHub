package com.example.E_Commerce.controller;

import com.example.E_Commerce.model.Coupon;
import com.example.E_Commerce.model.DiscountType;
import com.example.E_Commerce.repository.CouponRepository;
import com.example.E_Commerce.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/admin/coupon")
public class AdminCouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @GetMapping
    public String viewCoupons(Model model) {
        model.addAttribute("coupons", couponService.getAllCoupons());
        return "admin/coupon";
    }

    @PostMapping("/add")
    public String addCoupon(@RequestParam String code,
                            @RequestParam Double discount,
                            @RequestParam Double minimumAmount,
                            @RequestParam LocalDate expiryDate,
                            @RequestParam DiscountType discountType,
                            RedirectAttributes redirectAttributes) {

        if (couponService.existsByCode(code)) {
            redirectAttributes.addFlashAttribute("errorMessage", "A coupon with this discount already exists.");
            return "redirect:/admin/coupon";
        }else {
            redirectAttributes.addFlashAttribute("successMessage", "Coupon added successfully!");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDiscount(discount);
        coupon.setDiscountType(discountType);
        coupon.setMinimumAmount(minimumAmount);
        coupon.setExpiryDate(expiryDate);
        coupon.setIsActive(true);
        couponService.saveCoupon(coupon);


        return "redirect:/admin/coupon";
    }

    @PostMapping("/remove/{couponId}")
    public String removeCoupon( @PathVariable Integer couponId) {
        try {
            couponService.removeCoupon(couponId);
        } catch (Exception e) {

            return "redirect:/admin/coupon?error=CouponNotFound";
        }
        return "redirect:/admin/coupon";
    }






}
