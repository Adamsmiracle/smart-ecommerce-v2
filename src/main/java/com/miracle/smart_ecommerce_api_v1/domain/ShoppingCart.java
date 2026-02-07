package com.miracle.smart_ecommerce_api_v1.domain;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Shopping Cart domain model (POJO) - represents shopping_cart table.
 * One cart per user.
 * No JPA annotations - used with raw JDBC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ShoppingCart extends BaseModel {

    @NotNull(message = "User ID is required")
    private UUID userId;

    // Transient field for user (populated when needed)
    private transient User user;

    // Transient field for cart items (populated when needed)
    private transient java.util.List<CartItem> items;

    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    /**
     * Get total number of items in cart
     */
    public int getTotalItems() {
        if (items == null) return 0;
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Get total cart value
     */
    public java.math.BigDecimal getTotalValue() {
        if (items == null) return java.math.BigDecimal.ZERO;
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}

