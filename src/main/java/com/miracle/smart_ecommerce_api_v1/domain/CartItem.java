package com.miracle.smart_ecommerce_api_v1.domain;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cart Item domain model (POJO) - represents cart_item table.
 * No JPA annotations - used with raw JDBC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CartItem extends BaseModel {

    @NotNull(message = "Cart ID is required")
    private UUID cartId;

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Builder.Default
    private Integer quantity = 1;

    private LocalDateTime addedAt;

    // Transient fields for relationships (populated when needed)
    private transient ShoppingCart cart;
    private transient Product product;

    /**
     * Calculate subtotal for this cart item
     */
    public BigDecimal getSubtotal() {
        if (product == null || product.getPrice() == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Get unit price from product
     */
    public BigDecimal getUnitPrice() {
        return product != null ? product.getPrice() : null;
    }

    /**
     * Update quantity with validation
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.quantity = newQuantity;
    }
}

