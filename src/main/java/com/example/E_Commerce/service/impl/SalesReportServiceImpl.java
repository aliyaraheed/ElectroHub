package com.example.E_Commerce.service.impl;

import com.example.E_Commerce.dto.SalesReportResponse;
import com.example.E_Commerce.model.Order;
import com.example.E_Commerce.repository.OrderRepository;
import com.example.E_Commerce.service.SalesReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesReportServiceImpl implements SalesReportService {

    private final OrderRepository orderRepository;

    public SalesReportServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public SalesReportResponse generateSalesReport(LocalDate startDate, LocalDate endDate) {
        List<Order> orders = orderRepository.findAll();

        List<Order> filteredOrders = orders.stream()
                .filter(order -> {
                    LocalDate createdDate = order.getCreatedDate();
                    return createdDate != null &&
                            (startDate == null || !createdDate.isBefore(startDate)) &&
                            (endDate == null || !createdDate.isAfter(endDate));
                })
                .toList();

        Long totalOrders = (long) filteredOrders.size();

        Double totalRevenue = filteredOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .mapToDouble(orderItem -> orderItem.getProductPrice() * orderItem.getQuantity())
                .sum();

        Double couponDeductions = filteredOrders.stream()
                .mapToDouble(order -> order.getCouponDeduction() != null ? order.getCouponDeduction() : 0.0)
                .sum();

        SalesReportResponse response = new SalesReportResponse();
        response.setOrder(filteredOrders);
        response.setPeriod((startDate != null ? startDate : "N/A") + " to " + (endDate != null ? endDate : "N/A"));
        response.setTotalOrders(totalOrders);
        response.setTotalRevenue(totalRevenue - couponDeductions);
        response.setCouponDeductions(couponDeductions);

        return response;
    }


}
