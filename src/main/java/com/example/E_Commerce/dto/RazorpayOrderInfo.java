package com.example.E_Commerce.dto;

public class RazorpayOrderInfo {
    public RazorpayOrderInfo(Integer userId, Long addressId) {
        this.userId = userId;
        this.addressId = addressId;
    }

    private Integer userId;
    private Long addressId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }
}
