package com.miracle.smart_ecommerce_api_v1.domain.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for adding item to wishlist.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToWishlistRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Product ID is required")
    private UUID productId;
}

