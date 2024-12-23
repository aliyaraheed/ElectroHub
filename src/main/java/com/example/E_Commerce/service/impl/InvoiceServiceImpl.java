package com.example.E_Commerce.service.impl;

import com.example.E_Commerce.model.Order;
import com.example.E_Commerce.repository.OrderRepository;
import com.example.E_Commerce.service.InvoiceService;
import org.checkerframework.checker.units.qual.h;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    public OrderRepository orderRepository;

    @Override
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
