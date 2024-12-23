package com.example.E_Commerce.service;

import java.util.Map;

public interface RazorpayService {
    Map<String, Object> createOrder(Integer amount);
}
