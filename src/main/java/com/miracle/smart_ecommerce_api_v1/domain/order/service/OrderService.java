package com.miracle.smart_ecommerce_api_v1.domain.order.service;

import com.miracle.smart_ecommerce_api_v1.common.response.PageResponse;
import com.miracle.smart_ecommerce_api_v1.domain.order.dto.CreateOrderRequest;
import com.miracle.smart_ecommerce_api_v1.domain.order.dto.OrderResponse;

import java.util.UUID;

/**
 * Service interface for Order operations.
 */
public interface OrderService {

    /**
     * Create a new order
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Get order by ID
     */
    OrderResponse getOrderById(UUID id);

    /**
     * Get order by order number
     */
    OrderResponse getOrderByOrderNumber(String orderNumber);

    /**
     * Get all orders with pagination
     */
    PageResponse<OrderResponse> getAllOrders(int page, int size);

    /**
     * Get orders by user ID
     */
    PageResponse<OrderResponse> getOrdersByUserId(UUID userId, int page, int size);

    /**
     * Get orders by status
     */
    PageResponse<OrderResponse> getOrdersByStatus(String status, int page, int size);

    /**
     * Update order status
     */
    OrderResponse updateOrderStatus(UUID id, String status);

    /**
     * Update payment status
     */
    OrderResponse updatePaymentStatus(UUID id, String paymentStatus);

    /**
     * Cancel order
     */
    OrderResponse cancelOrder(UUID id);

    /**
     * Delete order
     */
    void deleteOrder(UUID id);

    /**
     * Count total orders
     */
    long countOrders();

    /**
     * Count orders by status
     */
    long countOrdersByStatus(String status);
}

