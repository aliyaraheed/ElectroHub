package com.example.E_Commerce.controller;

import com.example.E_Commerce.model.Order;
import com.example.E_Commerce.repository.OrderRepository;
import com.example.E_Commerce.service.InvoiceService;
import com.example.E_Commerce.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Controller
@RequestMapping("/user/order")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/invoice/{orderId}")
    public String getOrderDetails(@PathVariable Long orderId, Model model) {
        try {

            Order order = invoiceService.findOrderById(orderId);

            double grandTotal = order.getOrderItems().stream()
                    .mapToDouble(item -> item.getProductPrice() * item.getQuantity())
                    .sum();

            model.addAttribute("order",order);
            model.addAttribute("grandTotal", grandTotal);
            return "invoice";

        } catch (RuntimeException e) {
            return "invoice";
        }

    }
}