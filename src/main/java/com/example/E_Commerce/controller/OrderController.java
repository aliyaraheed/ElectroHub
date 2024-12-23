package com.example.E_Commerce.controller;

import com.example.E_Commerce.config.RazorpayConfig;
import com.example.E_Commerce.dto.RazorpayOrderInfo;
import com.example.E_Commerce.dto.RazorpaySuccessInfo;
import com.example.E_Commerce.exceptions.InsufficientStockException;
import com.example.E_Commerce.model.*;
import com.example.E_Commerce.repository.CartRepository;
import com.example.E_Commerce.repository.OrderRepository;
import com.example.E_Commerce.service.*;
import com.example.E_Commerce.service.other.CurrentUserDetailProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Controller
@Slf4j
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final CurrentUserDetailProvider currentUserDetailProvider;
    private final CartService cartService;
    private final UserService userService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final RazorpayConfig razorpayConfig;
    private final RazorpayService razorpayService;

    public OrderController(CurrentUserDetailProvider currentUserDetailProvider, CartService cartService, UserService userService, OrderService orderService, CouponService couponService, RazorpayConfig razorpayConfig, RazorpayService razorpayService) {
        this.currentUserDetailProvider = currentUserDetailProvider;
        this.cartService = cartService;
        this.userService = userService;
        this.orderService = orderService;
        this.couponService = couponService;
        this.razorpayConfig = razorpayConfig;
        this.razorpayService = razorpayService;
    }

    @GetMapping("/user/order")
    public String orderPage(Model model, Principal principal, RedirectAttributes attributes){
        Integer userId = currentUserDetailProvider.getUserId(principal);
        Optional<Cart> optionalCart = cartService.getCartByUserId(userId);
        if(!optionalCart.isPresent() || optionalCart.get().getItems().size() <= 0){
            attributes.addFlashAttribute("errorMsg", "Add cart items first.");
            return "redirect:/user/cart";
        }
        Double discountPrice = optionalCart.get().getItems().stream()
                .mapToDouble(i -> i.getProduct().getDiscountPrice() * i.getQuantity())
                .sum();

        List<Address> addresses = userService.findAddressByUserId(userId);
        model.addAttribute("addresses", addresses);
        model.addAttribute("cart", optionalCart.get());
        model.addAttribute("discountPrice", discountPrice);
        return "order-page.html";
    }

    @PostMapping("/user/order")
    public String performOrder(@RequestParam("address") Long addressId,
                               @RequestParam("paymentMethod") String paymentMethodString,
                               Principal principal,
                               RedirectAttributes attributes,
                               HttpSession session){
        Integer userId = currentUserDetailProvider.getUserId(principal);

        PaymentMethod paymentMethod;

        if (Objects.equals(paymentMethodString, "COD")){
            paymentMethod = PaymentMethod.COD;
        } else if (Objects.equals(paymentMethodString, "RAZORPAY")) {
            paymentMethod = PaymentMethod.RAZORPAY;
        } else if (Objects.equals(paymentMethodString, "WALLET")) {
            paymentMethod = PaymentMethod.WALLET;
        } else {
            attributes.addFlashAttribute("errorMsg", "Invalid payment method");
            return "redirect:/user/order";
        }


        try {
            if (paymentMethod == PaymentMethod.COD) {
                double totalPrice = orderService.calculateTotalPrice(userId);
                if (totalPrice > 1000) {
                    attributes.addFlashAttribute("errorMassage", "COD is applicable only for orders below ₹1000.");
                    return "redirect:/user/order";
                }else {
                    orderService.performOrder(addressId, paymentMethod, userId,false);
                    attributes.addFlashAttribute("success", "Order successfully completed!");
                    return "redirect:/user/orders";
                }

            } else if (paymentMethod == PaymentMethod.RAZORPAY) {
                boolean isValid = orderService.isValidOrder(userId, addressId);
                if(isValid){
                    RazorpayOrderInfo razorpayOrderInfo = new RazorpayOrderInfo(userId, addressId);
                    session.setAttribute("razorpayOrderInfo",razorpayOrderInfo);
                    return "redirect:/user/payment";
                }
                attributes.addFlashAttribute("errorMsg", "Something went wrong");
                return "redirect:/user/order";
            } else {
                attributes.addFlashAttribute("errorMsg", "Payment method not active. Please tray with another options!");
                return "redirect:/user/order";
            }

        } catch (Exception e){
            log.error("Error during ordering the cart {}",e.getMessage());
            attributes.addFlashAttribute("errorMsg", "Order failed, try again!");
            return "redirect:/user/order";
        }
    }

    @GetMapping("/user/payment")
    public String paymentPage(HttpSession session, Principal principal, Model model, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "User not authenticated");
            return "redirect:/user/login";
        }

        Integer userId = currentUserDetailProvider.getUserId(principal);
        RazorpayOrderInfo razorpayOrderInfo = (RazorpayOrderInfo) session.getAttribute("razorpayOrderInfo");

        if (razorpayOrderInfo == null || !Objects.equals(userId, razorpayOrderInfo.getUserId())) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong");
            return "redirect:/user/order";
        }

        model.addAttribute("razorpayOrderKey", razorpayConfig.getKeyId());
        Optional<Cart> optionalCart = cartService.getCartByUserId(userId);

        if (!optionalCart.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong");
            return "redirect:/user/order";
        }

        Cart cart = optionalCart.get();
        double totalPrice = cart.getItems().stream()
                .mapToDouble(i -> {
                    double discountedPrice = i.getProduct().getDiscountPrice() != null ? i.getProduct().getDiscountPrice() : 0.0;
                    double itemPrice = i.getProduct().getPrice();
                    return (itemPrice - discountedPrice >= 0 ? discountedPrice : itemPrice) * i.getQuantity();
                })
                .sum();


        Map<String, Object> razorpayOrder;
        try {
            razorpayOrder = razorpayService.createOrder((int) totalPrice);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Payment creation failed. Please try again later.");
            return "redirect:/user/order";
        }

        String razorpayOrderId = (String) razorpayOrder.get("id");


        log.info("Razorpay order id generated : {}", razorpayOrderId);
        model.addAttribute("razorpayOrderId", razorpayOrderId);

        return "paymentPage";
    }

    @PostMapping("/user/cancel-order/{id}")
    public String cancelOrder(@PathVariable("id") Long orderId, Principal principal, RedirectAttributes attributes) {
        Order order = orderService.getOrder(orderId);
        boolean isUserOrder = order.getUser().getId().equals(currentUserDetailProvider.getUserId(principal));

        if (!isUserOrder) {
            attributes.addFlashAttribute("errorMsg", "You are not authorized to cancel this order.");
            return "redirect:/user/orders";
        }

        if (!order.getOrderStatus().equals(OrderStatus.PENDING) && !order.getOrderStatus().equals(OrderStatus.PROCESSING) && !order.getOrderStatus().equals(OrderStatus.SHIPPED)) {
            attributes.addFlashAttribute("errorMsg", "Order cannot be canceled at this stage.");
            return "redirect:/user/orders";
        }

        orderService.cancelOrder(order);

        attributes.addFlashAttribute("success", "Order successfully canceled.");
        return "redirect:/user/orders";
    }

    @GetMapping("/admin/orders")
    public String listOrdersPage(Model model){
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin/orders-list";
    }

    @GetMapping("/admin/order/{id}")
    public String getSpecificOrder(@PathVariable("id") Long orderId, Model model){
        Order order = orderService.getSpecificOrder(orderId);
        model.addAttribute("order", order);
        return "admin/order-details";
    }

    @PostMapping("/admin/order/updateStatus")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId,
                                    @RequestParam("orderStatus") String orderStatus,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(orderId, orderStatus);
            redirectAttributes.addFlashAttribute("success", "Order status updated successfully.");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute("error", "Something went wrong!");
        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred while updating the order status.");
        }

        return "redirect:/admin/order/" + orderId;
    }


    @PostMapping("/admin/order/paymentStatusUpdate")
    public String updatePaymentStatus(@RequestParam("orderId") Long orderId,
                                    @RequestParam("paymentStatus") String paymentStatus,
                                    RedirectAttributes redirectAttributes) {
        try {

            orderService.updatePaymentStatus(orderId, paymentStatus);


            redirectAttributes.addFlashAttribute("success", "Order status updated successfully.");
        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute("error", "Something went wrong!");
        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred while updating the order status.");
        }

        return "redirect:/admin/order/" + orderId;
    }

    @PostMapping("/user/order/applyCoupon")
    public String applyCoupon(@RequestParam String couponCode,
                              @RequestParam Double cartTotal,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        Optional<Coupon> optionalCoupon = couponService.findByCode(couponCode);

        if (optionalCoupon.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid coupon code.");
            return "redirect:/user/order";
        }

        Coupon coupon = optionalCoupon.get();
        if (!coupon.getIsActive() || coupon.getExpiryDate().isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "This coupon is expired or inactive.");
            return "redirect:/user/order";
        }


        if (cartTotal < coupon.getMinimumAmount()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart total must be at least " + coupon.getMinimumAmount() + " to use this coupon.");
            return "redirect:/user/order";
        }


        Double discountAmount = 0.0;
        if (coupon.getDiscountType() == com.example.E_Commerce.model.DiscountType.PERCENTAGE) {
            discountAmount = cartTotal * (coupon.getDiscount() / 100);
        } else if (coupon.getDiscountType() == com.example.E_Commerce.model.DiscountType.FIXED_AMOUNT) {
            discountAmount = coupon.getDiscount();
        }

        discountAmount = Math.min(discountAmount, cartTotal);
        Double discountedTotal = cartTotal - discountAmount;


        redirectAttributes.addFlashAttribute("successMessage", "Coupon applied successfully! Discount: " + discountAmount);
        redirectAttributes.addFlashAttribute("discountedTotal", discountedTotal);
        redirectAttributes.addFlashAttribute("discountAmount", discountAmount);
        redirectAttributes.addFlashAttribute("appliedCouponCode", couponCode);

        return "redirect:/user/order";
    }

    @PostMapping("/user/order/removeCoupon")
    public String removeCoupon(RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("successMessage", "Coupon removed successfully.");
        redirectAttributes.addFlashAttribute("discountedTotal", null);
        redirectAttributes.addFlashAttribute("discountAmount", null);
        redirectAttributes.addFlashAttribute("appliedCouponCode", null);

        return "redirect:/user/order";
    }



}
