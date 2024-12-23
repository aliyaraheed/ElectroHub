package com.example.E_Commerce.service.impl;

import com.example.E_Commerce.exceptions.InsufficientStockException;
import com.example.E_Commerce.model.*;
import com.example.E_Commerce.repository.*;
import com.example.E_Commerce.service.OrderService;
import com.example.E_Commerce.service.ProductService;
import com.example.E_Commerce.service.RazorpayService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final RazorpayService razorpayService;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                            CartRepository cartRepository, CartItemRepository cartItemRepository,
                            ProductRepository productRepository, AddressRepository addressRepository,
                            UserRepository userRepository, ProductService productService, RazorpayService razorpayService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.productService = productService;
        this.razorpayService = razorpayService;
    }

    @Override
    public void performOrder(Long addressId, PaymentMethod paymentMethod, Integer userId, boolean isFailed) throws Exception {
        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        if (!optionalCart.isPresent() || optionalCart.get().getItems().isEmpty()) {
            throw new Exception("Cart or cart items not found");
        }

        UserDtls userDtls = userRepository.findById(userId).orElseThrow(() -> new Exception("User not found."));
        Cart cart = optionalCart.get();

        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getStock() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + item.getProduct().getTitle());
            }
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new Exception("Address not found."));

        LocalDate currentDate = LocalDate.now();

        List<OrderItem> orderItems = getOrderItemList(cart.getItems());
        Order order = new Order();
        order.setPaymentMethod(paymentMethod);
        order.setOrderStatus(OrderStatus.PENDING);
        if (paymentMethod == PaymentMethod.RAZORPAY && !isFailed){
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        } else if (paymentMethod == PaymentMethod.RAZORPAY && isFailed){
            order.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING);

        }
        order.setAddress(address);
        order.setUser(userDtls);
        order.setOrderItems(orderItems);
        order.setCreatedDate(currentDate);
        orderRepository.save(order);


        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        cartItemRepository.deleteAll(cart.getItems());
        cartRepository.delete(cart);
    }

    @Override
    public List<Order> getOrdersByUserId(Integer userId) {
        return orderRepository.findAllByUserId(userId);
    }

    @Override
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).get();
    }

    public void cancelOrder(Order order) {
        order.setOrderStatus(OrderStatus.CANCELLED);

        for (OrderItem orderItem : order.getOrderItems()) {

            productService.updateStock(orderItem.getProductId(), orderItem.getQuantity());
        }

        orderRepository.save(order);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getSpecificOrder(Long orderId) {
        return orderRepository.findById(orderId).get();
    }

    @Override
    public void updateOrderStatus(Long orderId, String orderStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new IllegalArgumentException("Order not found with ID: " + orderId)
        );

        if (OrderStatus.DELIVERED.equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("Cannot update status for delivered orders.");
        }

        OrderStatus newStatus;
        try {
             newStatus = OrderStatus.valueOf(orderStatus.toUpperCase());

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException("Invalid order status: " + orderStatus, e);
        }

        order.setOrderStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    public void updatePaymentStatus(Long orderId, String paymentStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new IllegalArgumentException("Order not found with ID: " + orderId)
        );

        try {
            PaymentStatus newPaymentStatus = PaymentStatus.valueOf(paymentStatus.toUpperCase());

            order.setPaymentStatus(newPaymentStatus);

            orderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment status: " + paymentStatus, e);
        }
    }

    @Override
    public String createRazorpayOrder(double amount) throws Exception {
        return "";
    }

    @Override
    public boolean isValidOrder(int userId, Long addressId) {
        try {
            Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
            if (!optionalCart.isPresent() || optionalCart.get().getItems().isEmpty()) {
                return false;
            }

            UserDtls userDtls = userRepository.findById(userId)
                    .orElseThrow(() -> new Exception("User not found."));

            Cart cart = optionalCart.get();
            for (CartItem item : cart.getItems()) {
                if (item.getProduct().getStock() < item.getQuantity()) {
                    return false;
                }
            }

            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new Exception("Address not found."));

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public double calculateTotalPrice(Integer userId) {
        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        if (!optionalCart.isPresent() || optionalCart.get().getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart or cart items not found");
        }

        Cart cart = optionalCart.get();
        return cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }



    private List<OrderItem> getOrderItemList(List<CartItem> cartItems) {
        return cartItems.stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductTitle(product.getTitle());
            orderItem.setProductDescription(product.getDescription());
            orderItem.setProductCategory(product.getCategory());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setProductDiscountPrice(product.getDiscountPrice());
            orderItem.setQuantity(cartItem.getQuantity());


            if (product.getImages() != null && !product.getImages().isEmpty()) {
                orderItem.setImage(product.getImages().get(0));
            } else {
                orderItem.setImage(null);
            }

            return orderItem;
        }).toList();
    }
}

