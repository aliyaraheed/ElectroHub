package com.example.E_Commerce.controller;

import com.example.E_Commerce.model.Coupon;
import com.example.E_Commerce.service.CouponService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@Controller
@RequestMapping("/coupons")
public class UserCouponController {

private final CouponService couponService;

    public UserCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/viewAll")
    public String viewAllCoupons(Model model) {

        List<Coupon> coupons = couponService.getAllActiveCoupons();
        model.addAttribute("coupons", coupons);
        return "/coupons";

    }
}
