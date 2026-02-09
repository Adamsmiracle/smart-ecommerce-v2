package com.miracle.smart_ecommerce_api_v1.domain.order.entity;

import com.miracle.smart_ecommerce_api_v1.domain.BaseModel;
import com.miracle.smart_ecommerce_api_v1.domain.product.entity.Product;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Order Item domain model (POJO) - represents order_item table.
 * No JPA annotations - used with raw JDBC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends BaseModel {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String productName;

    @Size(max = 50, message = "Product SKU cannot exceed 50 characters")
    private String productSku;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price must be non-negative")
    private BigDecimal unitPrice;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.00", message = "Total price must be non-negative")
    private BigDecimal totalPrice;

    // Transient fields for relationships (populated when needed)
    private transient CustomerOrder order;
    private transient Product product;

    /**
     * Calculate total price based on unit price and quantity
     */
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    /**
     * Create order item from product
     */
    public static OrderItem fromProduct(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        BigDecimal unitPrice = product.getPrice();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

        return OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .unitPrice(unitPrice)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .product(product)
                .build();
    }
}

