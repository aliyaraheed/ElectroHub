package com.example.E_Commerce.service;

import com.example.E_Commerce.model.Order;
import com.example.E_Commerce.model.PaymentMethod;

import java.util.List;

public interface OrderService {
    void performOrder(Long addressId, PaymentMethod paymentMethod, Integer userId, boolean isFailed) throws Exception;

    List<Order> getOrdersByUserId(Integer userId);

    Order getOrder(Long orderId);

    void cancelOrder(Order order);

    List<Order> getAllOrders();

    Order getSpecificOrder(Long orderId);

    void updateOrderStatus(Long orderId, String orderStatus);

    void updatePaymentStatus(Long orderId, String paymentStatus);

    String createRazorpayOrder(double amount) throws Exception;

    boolean isValidOrder(int userId, Long addressId);

    double calculateTotalPrice(Integer userId);






}
