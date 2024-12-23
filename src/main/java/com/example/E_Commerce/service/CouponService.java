package com.example.E_Commerce.service;

import com.example.E_Commerce.model.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponService {

    Coupon saveCoupon(Coupon coupon);

    Optional<Coupon> findByCode(String code);

    void deactivateCoupon(Integer couponId);

    List<Coupon> getAllCoupons();

    void removeCoupon(Integer id);

    boolean existsByCode(String code);

    List<Coupon> getAllActiveCoupons();
}
