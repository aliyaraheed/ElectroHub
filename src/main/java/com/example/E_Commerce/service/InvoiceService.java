package com.example.E_Commerce.service;

import com.example.E_Commerce.model.Order;

public interface InvoiceService {

    Order findOrderById(Long orderId);
}
