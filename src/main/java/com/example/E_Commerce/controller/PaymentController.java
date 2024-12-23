package com.example.E_Commerce.controller;

import com.example.E_Commerce.config.RazorpayConfig;
import com.example.E_Commerce.dto.RazorpayOrderInfo;
import com.example.E_Commerce.dto.RazorpaySuccessInfo;
import com.example.E_Commerce.model.PaymentMethod;
import com.example.E_Commerce.service.OrderService;
import com.example.E_Commerce.service.RazorpayService;
import com.example.E_Commerce.utils.Utils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final RazorpayService razorpayService;
    private final RazorpayConfig razorpayConfig;
    private final OrderService orderService;

    public PaymentController(RazorpayService razorpayService, RazorpayConfig razorpayConfig, OrderService orderService) {
        this.razorpayService = razorpayService;
        this.razorpayConfig = razorpayConfig;
        this.orderService = orderService;
    }

    @PostMapping("/paymentVerifyAndOrder")
    public ResponseEntity<String> paymentVerifyAndOrder(
            @RequestBody RazorpaySuccessInfo razorpaySuccessInfo, HttpSession session) {
        try {
            if(razorpaySuccessInfo == null || razorpaySuccessInfo.getOrderId() == null){
                return ResponseEntity.badRequest().body("Something went wrong");
            }
            RazorpayOrderInfo razorpayOrderInfo = (RazorpayOrderInfo) session.getAttribute("razorpayOrderInfo");
            if (razorpayOrderInfo == null) {
                log.warn("Razorpay order information not found in session.");
                return ResponseEntity.badRequest().body("Session not found. Please retry.");
            }

            orderService.performOrder(
                    razorpayOrderInfo.getAddressId(),
                    PaymentMethod.RAZORPAY,
                    razorpayOrderInfo.getUserId(),
                    false
            );
            session.removeAttribute("razorpayOrderInfo");

            log.info("Order processing completed successfully for userId: {}", razorpayOrderInfo.getUserId());
            return ResponseEntity.ok("Payment Verified Successfully");
        } catch (Exception e) {
            log.error("Unexpected error during payment verification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment verification failed. Please contact support.");
        }
    }


    @PostMapping("/paymentFailureHandler")
    public ResponseEntity<String> paymentFailureHandler(
            @RequestBody RazorpaySuccessInfo razorpaySuccessInfo, HttpSession session) {
        try {
            if(razorpaySuccessInfo == null || razorpaySuccessInfo.getOrderId() == null){
                return ResponseEntity.badRequest().body("Something went wrong");
            }
            RazorpayOrderInfo razorpayOrderInfo = (RazorpayOrderInfo) session.getAttribute("razorpayOrderInfo");
            if (razorpayOrderInfo == null) {
                log.warn("Razorpay order information not found in session.");
                return ResponseEntity.badRequest().body("Session not found. Please retry.");
            }

            orderService.performOrder(
                    razorpayOrderInfo.getAddressId(),
                    PaymentMethod.RAZORPAY,
                    razorpayOrderInfo.getUserId(),
                    true
            );
            session.removeAttribute("razorpayOrderInfo");

            log.info("Order processing completed successfully for userId: {}", razorpayOrderInfo.getUserId());
            return ResponseEntity.ok("Payment Verified Successfully");
        } catch (Exception e) {
            log.error("Unexpected error during payment verification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment verification failed. Please contact support.");
        }
    }







}
