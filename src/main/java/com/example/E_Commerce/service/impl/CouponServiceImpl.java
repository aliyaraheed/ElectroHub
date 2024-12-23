package com.example.E_Commerce.service.impl;

import com.example.E_Commerce.model.Coupon;
import com.example.E_Commerce.repository.CouponRepository;
import com.example.E_Commerce.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Override
    public Coupon saveCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return couponRepository.findByCode(code);
    }

    @Override
    public void deactivateCoupon(Integer couponId) {
        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        coupon.setIsActive(false);
        couponRepository.save(coupon);
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public void removeCoupon(Integer id) {
        couponRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        Optional<Coupon> coupon = couponRepository.findByCode(code);
        return coupon.isPresent();
    }

    @Override
    public List<Coupon> getAllActiveCoupons() {
        return couponRepository.findByIsActiveTrueAndExpiryDateAfter(LocalDate.now());
    }


}
