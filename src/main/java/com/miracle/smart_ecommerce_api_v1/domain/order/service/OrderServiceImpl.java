package com.miracle.smart_ecommerce_api_v1.domain.order.service;

import java.time.OffsetDateTime;

import com.miracle.smart_ecommerce_api_v1.common.response.PageResponse;
import com.miracle.smart_ecommerce_api_v1.domain.order.entity.CustomerOrder;
import com.miracle.smart_ecommerce_api_v1.domain.order.entity.CustomerOrder.OrderStatus;
import com.miracle.smart_ecommerce_api_v1.domain.order.entity.CustomerOrder.PaymentStatus;
import com.miracle.smart_ecommerce_api_v1.domain.order.entity.OrderItem;
import com.miracle.smart_ecommerce_api_v1.domain.order.repository.OrderItemRepository;
import com.miracle.smart_ecommerce_api_v1.domain.order.repository.OrderRepository;
import com.miracle.smart_ecommerce_api_v1.domain.product.entity.Product;
import com.miracle.smart_ecommerce_api_v1.domain.product.repository.ProductRepository;
import com.miracle.smart_ecommerce_api_v1.domain.user.entity.Address;
import com.miracle.smart_ecommerce_api_v1.domain.user.entity.User;
import com.miracle.smart_ecommerce_api_v1.domain.order.dto.CreateOrderRequest;
import com.miracle.smart_ecommerce_api_v1.domain.order.dto.OrderResponse;
import com.miracle.smart_ecommerce_api_v1.domain.order.dto.UpdateOrderRequest;
import com.miracle.smart_ecommerce_api_v1.domain.user.repository.AddressRepository;
import com.miracle.smart_ecommerce_api_v1.domain.user.repository.UserRepository;
import com.miracle.smart_ecommerce_api_v1.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.miracle.smart_ecommerce_api_v1.config.CacheConfig.*;

/**
 * Implementation of OrderService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final CacheManager cacheManager;

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

        OrderResponse response = mapToResponse(savedOrder);

        // Update caches with new order
        Cache byIdCache = cacheManager.getCache(ORDERS_CACHE);
        if (byIdCache != null) {
            byIdCache.put("id:" + savedOrder.getId(), response);
        }

        Cache byNumberCache = cacheManager.getCache(ORDERS_CACHE);
        if (byNumberCache != null) {
            byNumberCache.put("number:" + savedOrder.getOrderNumber(), response);
        }

        // Clear list cache
        evictCache(ORDERS_CACHE);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = ORDERS_CACHE, key = "'id:' + #id")
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

        String orderNumber = order.getOrderNumber();

        // Validate status transition
        validateStatusTransition(order.getStatus(), status);

        orderRepository.updateStatus(id, status.toLowerCase());

        // Refresh order
        order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));

        log.info("Order status updated successfully: {}", id);
        OrderResponse response = mapToResponseWithDetails(order);

        // Update caches
        Cache byIdCache = cacheManager.getCache(ORDERS_CACHE);
        if (byIdCache != null) {
            byIdCache.put("id:" + id, response);
        }

        Cache byNumberCache = cacheManager.getCache(ORDERS_CACHE);
        if (byNumberCache != null && orderNumber != null) {
            byNumberCache.put("number:" + orderNumber, response);
        }

        // Clear list cache
        evictCache(ORDERS_CACHE);

        return response;
    }

    @Override
    @Transactional
    public OrderResponse updatePaymentStatus(UUID id, String paymentStatus) {
        log.info("Updating payment status for order: {} to {}", id, paymentStatus);

        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));

        String orderNumber = order.getOrderNumber();

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
        OrderResponse response = mapToResponseWithDetails(order);

        // Update caches
        Cache byIdCache = cacheManager.getCache(ORDERS_CACHE);
        if (byIdCache != null) {
            byIdCache.put("id:" + id, response);
        }

        Cache byNumberCache = cacheManager.getCache(ORDERS_CACHE);
        if (byNumberCache != null && orderNumber != null) {
            byNumberCache.put("number:" + orderNumber, response);
        }

        // Clear list cache
        evictCache(ORDERS_CACHE);

        return response;
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID id) {
        log.info("Cancelling order: {}", id);

        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));

        String orderNumber = order.getOrderNumber();

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
            // Evict product caches for updated stock
            Cache productCache = cacheManager.getCache(PRODUCTS_CACHE);
            if (productCache != null) {
                productCache.evict("id:" + item.getProductId());
            }
        }

        // Refresh order
        order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));
        order.setCancelledAt(OffsetDateTime.now());

        log.info("Order cancelled successfully: {}", id);
        OrderResponse response = mapToResponseWithDetails(order);

        // Update order caches
        Cache byIdCache = cacheManager.getCache(ORDERS_CACHE);
        if (byIdCache != null) {
            byIdCache.put("id:" + id, response);
        }

        Cache byNumberCache = cacheManager.getCache(ORDERS_CACHE);
        if (byNumberCache != null && orderNumber != null) {
            byNumberCache.put("number:" + orderNumber, response);
        }

        return response;
    }

    @Override
    @Transactional
    public void deleteOrder(UUID id) {
        log.info("Deleting order: {}", id);

        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));

        String orderNumber = order.getOrderNumber();

        // Delete order items first
        orderItemRepository.deleteByOrderId(id);

        // Delete order
        orderRepository.deleteById(id);
        log.info("Order deleted successfully: {}", id);

        // Evict from id and number caches
        Cache byIdCache = cacheManager.getCache(ORDERS_CACHE);
        if (byIdCache != null) {
            byIdCache.evict("id:" + id);
        }

        Cache byNumberCache = cacheManager.getCache(ORDERS_CACHE);
        if (byNumberCache != null && orderNumber != null) {
            byNumberCache.evict("number:" + orderNumber);
        }

        // Clear list cache
        evictCache(ORDERS_CACHE);
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
                .createdAt(OffsetDateTime.from(order.getCreatedAt()))
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

    /**
     * Helper method to evict all entries from a cache
     */
    private void evictCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(UUID id, UpdateOrderRequest request) {
        log.info("Updating order {} with request: {}", id, request);

        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));

        boolean changed = false;
        String orderNumber = order.getOrderNumber();

        if (request.getPaymentMethodId() != null && !request.getPaymentMethodId().equals(order.getPaymentMethodId())) {
            order.setPaymentMethodId(request.getPaymentMethodId());
            changed = true;
        }
        if (request.getShippingAddressId() != null && !request.getShippingAddressId().equals(order.getShippingAddressId())) {
            // verify address belongs to user? optional
            order.setShippingAddressId(request.getShippingAddressId());
            changed = true;
        }
        if (request.getShippingMethodId() != null && !request.getShippingMethodId().equals(order.getShippingMethodId())) {
            order.setShippingMethodId(request.getShippingMethodId());
            changed = true;
        }
        if (request.getCustomerNotes() != null && !request.getCustomerNotes().equals(order.getCustomerNotes())) {
            order.setCustomerNotes(request.getCustomerNotes());
            changed = true;
        }

        // Process item updates if provided
        if (request.getItems() != null) {
            // Load existing items
            List<OrderItem> existingItems = orderItemRepository.findByOrderId(id);

            // Map existing items by id for quick lookup
            java.util.Map<java.util.UUID, OrderItem> existingById = existingItems.stream()
                    .filter(it -> it.getId() != null)
                    .collect(Collectors.toMap(OrderItem::getId, it -> it));

            // We'll build new list of items to persist/keep
            List<OrderItem> resultingItems = new ArrayList<>();

            // Track stock changes: productId -> stockDelta (negative means reduce stock)
            java.util.Map<UUID, Integer> stockDeltas = new java.util.HashMap<>();

            for (UpdateOrderRequest.OrderItemUpdateRequest itemReq : request.getItems()) {
                if (itemReq.getId() != null && existingById.containsKey(itemReq.getId())) {
                    // Update existing item
                    OrderItem existing = existingById.get(itemReq.getId());
                    if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                        // delete this item: stock should be restored by +existing.quantity
                        stockDeltas.merge(existing.getProductId(), existing.getQuantity(), Integer::sum);
                        // don't add to resultingItems
                        changed = true;
                        continue;
                    }

                    if (!existing.getQuantity().equals(itemReq.getQuantity())) {
                        int qtyDiff = itemReq.getQuantity() - existing.getQuantity();
                        // reduce stock by qtyDiff (can be negative to restore stock)
                        stockDeltas.merge(existing.getProductId(), -qtyDiff, Integer::sum);
                        existing.setQuantity(itemReq.getQuantity());
                        existing.calculateTotalPrice();
                        changed = true;
                    }
                    resultingItems.add(existing);
                } else {
                    // New item to add
                    if (itemReq.getProductId() == null || itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                        // ignore invalid
                        continue;
                    }
                    Product product = productRepository.findById(itemReq.getProductId())
                            .orElseThrow(() -> ResourceNotFoundException.forResource("Product", itemReq.getProductId()));

                    // validate stock
                    if (product.getStockQuantity() < itemReq.getQuantity()) {
                        throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
                    }

                    OrderItem newItem = OrderItem.fromProduct(product, itemReq.getQuantity());
                    newItem.setOrderId(id);
                    // will be saved below
                    resultingItems.add(newItem);
                    // reduce stock by quantity
                    stockDeltas.merge(product.getId(), -itemReq.getQuantity(), Integer::sum);
                    changed = true;
                }
            }

            // Apply stock deltas
            for (java.util.Map.Entry<UUID, Integer> e : stockDeltas.entrySet()) {
                UUID pid = e.getKey();
                int delta = e.getValue();
                if (delta == 0) continue;
                productRepository.updateStock(pid, delta);
            }

            // Persist item changes: delete all existing and re-insert resultingItems for simplicity
            orderItemRepository.deleteByOrderId(id);
            for (OrderItem item : resultingItems) {
                item.setOrderId(id);
                OrderItem saved = orderItemRepository.save(item);
                // update id if needed
                item.setId(saved.getId());
            }

            // attach items to order
            order.setOrderItems(resultingItems);

            // Recalculate subtotal/total
            BigDecimal newSubtotal = resultingItems.stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setSubtotal(newSubtotal);
            order.setTotal(newSubtotal.add(order.getShippingCost() == null ? BigDecimal.ZERO : order.getShippingCost()));
        }

        if (!changed) {
            log.debug("No changes detected for order {}", id);
            return mapToResponseWithDetails(order);
        }

        // Persist order changes
        CustomerOrder updated = orderRepository.update(order);

        // Refresh and prepare response
        updated = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Order", id));

        OrderResponse response = mapToResponseWithDetails(updated);

        // Update caches
        Cache byIdCache = cacheManager.getCache(ORDERS_CACHE);
        if (byIdCache != null) {
            byIdCache.put("id:" + id, response);
        }
        Cache byNumberCache = cacheManager.getCache(ORDERS_CACHE);
        if (byNumberCache != null && orderNumber != null) {
            byNumberCache.put("number:" + orderNumber, response);
        }

        // Clear list cache
        evictCache(ORDERS_CACHE);

        log.info("Order {} updated successfully", id);
        return response;
    }
}
