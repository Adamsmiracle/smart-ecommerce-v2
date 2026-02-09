package com.miracle.smart_ecommerce_api_v1.domain.order.entity;

import com.miracle.smart_ecommerce_api_v1.domain.BaseModel;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Shipping Method domain model (POJO) - represents shipping_method table.
 * No JPA annotations - used with raw JDBC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ShippingMethod extends BaseModel {

    @NotBlank(message = "Shipping method name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    private BigDecimal price;

    @Min(value = 0, message = "Estimated days cannot be negative")
    private Integer estimatedDays;

    @Builder.Default
    private Boolean isActive = true;

    /**
     * Get formatted estimated delivery
     */
    public String getEstimatedDelivery() {
        if (estimatedDays == null) {
            return "Contact for estimate";
        }
        if (estimatedDays == 0) {
            return "Same day delivery";
        }
        if (estimatedDays == 1) {
            return "Next day delivery";
        }
        return estimatedDays + " business days";
    }
}

