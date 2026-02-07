package com.miracle.smart_ecommerce_api_v1.service.impl;

import com.miracle.smart_ecommerce_api_v1.common.response.PageResponse;
import com.miracle.smart_ecommerce_api_v1.domain.*;
import com.miracle.smart_ecommerce_api_v1.domain.CustomerOrder.OrderStatus;
import com.miracle.smart_ecommerce_api_v1.domain.CustomerOrder.PaymentStatus;
import com.miracle.smart_ecommerce_api_v1.dto.request.CreateOrderRequest;
import com.miracle.smart_ecommerce_api_v1.dto.response.OrderResponse;
import com.miracle.smart_ecommerce_api_v1.exception.ResourceNotFoundException;
import com.miracle.smart_ecommerce_api_v1.repository.*;
import com.miracle.smart_ecommerce_api_v1.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of OrderService.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository,
                           AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        // Verify user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> ResourceNotFoundException.forResource("User", request.getUserId()));

        // Generate order number
        String orderNumber = CustomerOrder.generateOrderNumber();

        // Calculate order totals from items
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> ResourceNotFoundException.forResource("Product", itemRequest.getProductId()));

            // Validate stock
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(totalPrice);

            OrderItem orderItem = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .unitPrice(unitPrice)
                    .quantity(itemRequest.getQuantity())
                    .totalPrice(totalPrice)
                    .build();

            orderItems.add(orderItem);
        }

        // Get shipping cost (default to 0 if no shipping method)
        BigDecimal shippingCost = BigDecimal.ZERO;
        // TODO: Get actual shipping cost from shipping method if provided

        BigDecimal total = subtotal.add(shippingCost);

        // Create order
        CustomerOrder order = CustomerOrder.builder()
                .userId(request.getUserId())
                .orderNumber(orderNumber)
                .status(OrderStatus.PENDING.name().toLowerCase())
                .paymentStatus(PaymentStatus.PENDING.name().toLowerCase())
                .paymentMethodId(request.getPaymentMethodId())
                .shippingAddressId(request.getShippingAddressId())
                .shippingMethodId(request.getShippingMethodId())
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .total(total)
                .customerNotes(request.getCustomerNotes())
                .build();

        CustomerOrder savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {} and order number: {}", savedOrder.getId(), orderNumber);

        // Save order items
        for (OrderItem item : orderItems) {
            item.setOrderId(savedOrder.getId());
            orderItemRepository.save(item);
        }

        // Update product stock
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            productRepository.updateStock(itemRequest.getProductId(), -itemRequest.getQuantity());
        }

        // Load items for response
        savedOrder.setOrderItems(orderItemRepository.findByOrderId(savedOrder.getId()));
        savedOrder.setUser(user);

        // Load shipping address if present
        if (savedOrder.getShippingAddressId() != null) {
            addressRepository.findById(savedOrder.getShippingAddressId())
                    .ifPresent(savedOrder::setShippingAddress);
        }

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        log.debug("Getting order by ID: {}", id);
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));
        return mapToResponseWithDetails(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        log.debug("Getting order by order number: {}", orderNumber);
        CustomerOrder order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return mapToResponseWithDetails(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(int page, int size) {
        log.debug("Getting all orders - page: {}, size: {}", page, size);
        List<CustomerOrder> orders = orderRepository.findAll(page, size);
        long total = orderRepository.count();

        List<OrderResponse> responses = orders.stream()
                .map(this::mapToResponseWithDetails)
                .collect(Collectors.toList());

        return PageResponse.of(responses, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByUserId(UUID userId, int page, int size) {
        log.debug("Getting orders for user: {} - page: {}, size: {}", userId, page, size);
        List<CustomerOrder> orders = orderRepository.findByUserId(userId, page, size);
        long total = orderRepository.countByUserId(userId);

        List<OrderResponse> responses = orders.stream()
                .map(this::mapToResponseWithDetails)
                .collect(Collectors.toList());

        return PageResponse.of(responses, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByStatus(String status, int page, int size) {
        log.debug("Getting orders by status: {} - page: {}, size: {}", status, page, size);
        List<CustomerOrder> orders = orderRepository.findByStatus(status.toLowerCase(), page, size);
        long total = orderRepository.countByStatus(status.toLowerCase());

        List<OrderResponse> responses = orders.stream()
                .map(this::mapToResponseWithDetails)
                .collect(Collectors.toList());

        return PageResponse.of(responses, page, size, total);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID id, String status) {
        log.info("Updating order status: {} to {}", id, status);

        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));

        // Validate status transition
        validateStatusTransition(order.getStatus(), status);

        orderRepository.updateStatus(id, status.toLowerCase());

        // Refresh order
        order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));

        log.info("Order status updated successfully: {}", id);
        return mapToResponseWithDetails(order);
    }

    @Override
    @Transactional
    public OrderResponse updatePaymentStatus(UUID id, String paymentStatus) {
        log.info("Updating payment status for order: {} to {}", id, paymentStatus);

        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));

        orderRepository.updatePaymentStatus(id, paymentStatus.toLowerCase());

        // If payment is successful, update order status to confirmed
        if ("paid".equalsIgnoreCase(paymentStatus) &&
            OrderStatus.PENDING.name().equalsIgnoreCase(order.getStatus())) {
            orderRepository.updateStatus(id, OrderStatus.CONFIRMED.name().toLowerCase());
        }

        // Refresh order
        order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));

        log.info("Payment status updated successfully for order: {}", id);
        return mapToResponseWithDetails(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID id) {
        log.info("Cancelling order: {}", id);

        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));

        // Check if order can be cancelled
        if (!order.canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled. Current status: " + order.getStatus());
        }

        // Update status to cancelled
        orderRepository.updateStatus(id, OrderStatus.CANCELLED.name().toLowerCase());

        // Restore product stock
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        for (OrderItem item : items) {
            productRepository.updateStock(item.getProductId(), item.getQuantity());
        }

        // Refresh order
        order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));
        order.setCancelledAt(LocalDateTime.now());

        log.info("Order cancelled successfully: {}", id);
        return mapToResponseWithDetails(order);
    }

    @Override
    @Transactional
    public void deleteOrder(UUID id) {
        log.info("Deleting order: {}", id);
        if (!orderRepository.existsById(id)) {
            throw ResourceNotFoundException.forResource("Order", id);
        }

        // Delete order items first
        orderItemRepository.deleteByOrderId(id);

        // Delete order
        orderRepository.deleteById(id);
        log.info("Order deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrders() {
        return orderRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrdersByStatus(String status) {
        return orderRepository.countByStatus(status.toLowerCase());
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private OrderResponse mapToResponse(CustomerOrder order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .customerName(order.getUser() != null ? order.getUser().getFullName() : null)
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(order.getSubtotal())
                .shippingCost(order.getShippingCost())
                .total(order.getTotal())
                .itemCount(order.getItemCount())
                .customerNotes(order.getCustomerNotes())
                .createdAt(order.getCreatedAt())
                .cancelledAt(order.getCancelledAt())
                .items(mapOrderItems(order.getOrderItems()))
                .shippingAddress(mapShippingAddress(order.getShippingAddress()))
                .build();
    }

    private OrderResponse mapToResponseWithDetails(CustomerOrder order) {
        // Load order items
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        order.setOrderItems(items);

        // Load user
        userRepository.findById(order.getUserId()).ifPresent(order::setUser);

        // Load shipping address
        if (order.getShippingAddressId() != null) {
            addressRepository.findById(order.getShippingAddressId())
                    .ifPresent(order::setShippingAddress);
        }

        return mapToResponse(order);
    }

    private List<OrderResponse.OrderItemResponse> mapOrderItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return items.stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productSku(item.getProductSku())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());
    }

    private OrderResponse.ShippingAddressResponse mapShippingAddress(Address address) {
        if (address == null) {
            return null;
        }
        return OrderResponse.ShippingAddressResponse.builder()
                .id(address.getId())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .region(address.getRegion())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .fullAddress(buildFullAddress(address))
                .build();
    }

    private String buildFullAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        if (address.getAddressLine() != null && !address.getAddressLine().isEmpty()) {
            sb.append(address.getAddressLine()).append(", ");
        }
        sb.append(address.getCity());
        if (address.getRegion() != null && !address.getRegion().isEmpty()) {
            sb.append(", ").append(address.getRegion());
        }
        if (address.getPostalCode() != null && !address.getPostalCode().isEmpty()) {
            sb.append(" ").append(address.getPostalCode());
        }
        sb.append(", ").append(address.getCountry());
        return sb.toString();
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Define valid status transitions
        // PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED
        // Any status -> CANCELLED (if canBeCancelled)

        String current = currentStatus.toLowerCase();
        String next = newStatus.toLowerCase();

        if (current.equals(next)) {
            return; // No change
        }

        boolean validTransition = switch (current) {
            case "pending" -> next.equals("confirmed") || next.equals("cancelled");
            case "confirmed" -> next.equals("processing") || next.equals("cancelled");
            case "processing" -> next.equals("shipped") || next.equals("cancelled");
            case "shipped" -> next.equals("delivered");
            case "delivered", "cancelled" -> false; // Terminal states
            default -> false;
        };

        if (!validTransition) {
            throw new IllegalStateException(
                    String.format("Invalid status transition from '%s' to '%s'", currentStatus, newStatus));
        }
    }
}

